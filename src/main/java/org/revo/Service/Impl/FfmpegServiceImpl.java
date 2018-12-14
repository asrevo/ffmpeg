package org.revo.Service.Impl;

import com.comcast.viper.hlsparserj.PlaylistVersion;
import com.comcast.viper.hlsparserj.tags.UnparsedTag;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.revo.Config.Env;
import org.revo.Domain.Index;
import org.revo.Domain.IndexImpl;
import org.revo.Domain.Master;
import org.revo.Service.FfmpegService;
import org.revo.Service.S3Service;
import org.revo.Service.SignedUrlService;
import org.revo.Service.TempFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.comcast.viper.hlsparserj.PlaylistFactory.parsePlaylist;
import static java.util.Comparator.comparingInt;
import static org.revo.Domain.IndexImpl.list;
import static org.revo.Domain.Resolution.getLess;
import static org.revo.Util.FileUtils.read;

@Service
@Slf4j
public class FfmpegServiceImpl implements FfmpegService {
    @Autowired
    private S3Service s3Service;
    @Autowired
    private FFmpegExecutor executor;
    @Autowired
    private FFprobe fFprobe;
    @Autowired
    private SignedUrlService signedUrlService;
    @Autowired
    private TempFileService tempFileService;
    @Autowired
    private Env env;
    @Value("${logo}")
    private String logo;


    @Override
    public Master convert(Master payload) throws IOException {
        Path source = s3Service.pull("convert", payload.getId());
        log.info("source " + source.toFile().toString() + "        " + source.toFile().length() + "        " + source.toFile().getFreeSpace() + "         ");
        long start = System.currentTimeMillis();
        IndexImpl index = payload.getImpls().get(0);
        Path converted = doConversion(fFprobe.probe(signedUrlService.generate(env.getBuckets().get("video"), payload.getId())), index);
        long execution = System.currentTimeMillis() - start;
        log.info("take " + source.toFile().toString() + " " + execution);
        index.setExecution(execution);
        s3Service.pushMedia(index.getIndex(), converted.toFile());
        log.info("converted " + converted.toFile().toString() + "        " + converted.toFile().length() + "        " + converted.toFile().getFreeSpace() + "         ");
        payload.setImpls(Collections.singletonList(index));
        return payload;
    }

    private Path ddoConversion(FFmpegProbeResult probe, Master master) {
        Path out = tempFileService.tempFile("hls", master.getId() + File.separator + master.getImpls().get(0).getIndex() + File.separator + "index.m3u8");
        tempFileService.mkdir(out.getParent().getParent());
        tempFileService.mkdir(out.getParent());
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(probe)
                .addOutput(out.toString())
                .setFormat("m3u8")
                .addExtraArgs("-f", "hls")
                .addExtraArgs("-codec:", "copy")
                .addExtraArgs("-start_number", "0")
                .addExtraArgs("-hls_time", "10")
                .addExtraArgs("-hls_list_size", "0")
                .addExtraArgs("-hls_enc", "1")
                .addExtraArgs("-hls_enc_key", master.getSecret())
                .addExtraArgs("-hls_enc_key_url", master.getId() + ".key\\")
                .addExtraArgs("-hls_enc_iv", UUID.randomUUID().toString().replace("-", ""))
                .addExtraArgs("-master_pl_name", master.getId() + ".m3u8")
                .done();
        FFmpegJob job = executor.createJob(builder);
        job.run();
        log.info("job " + job.getState());
        return out;
    }

    @Override
    public Index hls(Master master) throws IOException {
        Path converted = ddoConversion(fFprobe.probe(signedUrlService.generate(env.getBuckets().get("video"), master.getImpls().get(0).getIndex())), master);
        Index index = new Index();
        index.setMaster(master.getId());
        index.setId(master.getImpls().get(0).getIndex());
        index.setExecution(master.getImpls().get(0).getExecution());
        Optional<UnparsedTag> masterTag = getMasterTag(converted.getParent().resolve(master.getId() + ".m3u8"));
        masterTag.ifPresent(it -> {
            index.setStream(read(converted));
            index.setAverage_bandwidth(it.getAttributes().get("AVERAGE-BANDWIDTH"));
            index.setBandwidth(it.getAttributes().get("BANDWIDTH"));
            index.setCodecs(it.getAttributes().get("CODECS"));
            index.setResolution(it.getAttributes().get("RESOLUTION"));
        });
        Path base = converted.getParent().getParent().getParent().getParent();
        s3Service.push(base, converted.getParent().getParent());
        return index;
    }

    private Optional<UnparsedTag> getMasterTag(Path path) {
        return parsePlaylist(PlaylistVersion.TWELVE, read(path)).getTags().stream()
                .filter(it -> it.getTagName().equalsIgnoreCase("EXT-X-STREAM-INF"))
                .findAny();
    }

    @Override
    public Master queue(Master master) throws IOException {
        FFmpegProbeResult probe = fFprobe.probe(signedUrlService.generate(env.getBuckets().get("video"), master.getId()));
        for (Path png : image(probe, master.getId(), "png")) {
            File file = png.toFile();
            s3Service.pushImage(png.getFileName().toString(), file);
            file.delete();
        }
        for (Path png : image(probe, master.getId(), "webp")) {
            File file = png.toFile();
            s3Service.pushImage(png.getFileName().toString(), file);
            file.delete();
        }
        return info(probe, master);
    }

    private Master info(FFmpegProbeResult probe, Master master) {
        master.setResolution(probe.getStreams().stream().filter(it -> it.codec_type == FFmpegStream.CodecType.VIDEO).max(comparingInt(o -> o.height * o.width)).
                map(it -> ((it.width / 2) * 2) + "x" + ((it.height / 2) * 2)).orElse(""));
        master.setTime(probe.getFormat().duration);
        master.setMp4(probe.getFormat().format_long_name.equalsIgnoreCase("QuickTime / MOV"));
        master.setImage(signedUrlService.getUrl(master.getId(), "thumb"));
        master.setImpls(list(getLess(master.getResolution())));
        return master;
    }

    private List<Path> image(FFmpegProbeResult probe, String id, String type) throws IOException {
        Path thumbnail = tempFileService.tempFile("queue", id + (type.equals("png") ? "_%d" : "") + "." + type);
        probe.getStreams().stream().filter(it -> it.codec_type == FFmpegStream.CodecType.VIDEO)
                .findFirst()
                .ifPresent(it -> {
                    FFmpegOutputBuilder fFmpegOutputBuilder = new FFmpegBuilder()
                            .setInput(probe)
                            .addOutput(thumbnail.toString());
                    if (type.equals("webp")) {
                        long millis = ((long) it.duration) * 1000;
                        fFmpegOutputBuilder.addExtraArgs("-ss", format(millis / 2)).addExtraArgs("-t", format(3 * 1000)).addExtraArgs("-loop", "0").setVideoFilter("select='gte(n\\,10)',scale=320:-1");
                    }
                    if (type.equals("png")) {
                        fFmpegOutputBuilder.setVideoFilter("fps='(30/60)',select='gte(n\\,10)',scale=320:-1");
                    }
                    executor.createJob(fFmpegOutputBuilder.done()).run();
                });
        return Files.walk(thumbnail.getParent()).filter(it -> Files.isRegularFile(it)).collect(Collectors.toList());
    }

    private static String format(long millis) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    private Path doConversion(FFmpegProbeResult probe, IndexImpl index) throws IOException {
        Path out = tempFileService.tempFile("convert", index.getIndex() + UUID.randomUUID().toString().replace("-", ""));
        String[] split = index.getResolution().split("x");
        Integer width = Integer.valueOf(split[0]);
        Integer height = Integer.valueOf(split[1]);
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(probe)
                .addOutput(out.toString())
                .setFormat("mp4")
                .setVideoFilter("drawtext=\'text=\'" + logo + "\': fontsize=24 : fontcolor=white: x=((w/20)): y=((h/20))\'")
                .setVideoResolution(width, height)
                .done();
        FFmpegJob job = executor.createJob(builder);
        job.run();
        log.info("job " + job.getState());
        return out;
    }
}
