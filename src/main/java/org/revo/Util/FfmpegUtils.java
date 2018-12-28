package org.revo.Util;

import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.revo.Domain.IndexImpl;
import org.revo.Domain.Master;
import org.revo.Domain.Status;
import org.revo.Service.SignedUrlService;
import org.revo.Service.TempFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static org.revo.Domain.IndexImpl.list;
import static org.revo.Domain.Resolution.getLess;
import static org.revo.Util.Utils.format;

@Component
@Slf4j
public class FfmpegUtils {
    @Autowired
    private TempFileService tempFileService;
    @Autowired
    private SignedUrlService signedUrlService;
    @Autowired
    private FFmpegExecutor executor;
    @Value("${logo}")
    private String logo;

    public Master info(FFmpegProbeResult probe, Master master) {
        master.setResolution(probe.getStreams().stream().filter(it -> it.codec_type == FFmpegStream.CodecType.VIDEO).max(comparingInt(o -> o.height * o.width)).
                map(it -> ((it.width / 2) * 2) + "x" + ((it.height / 2) * 2)).orElse(""));
        master.setTime(probe.getFormat().duration);
        master.setImage(signedUrlService.getUrl(master.getFile() + "/" + master.getId() + "/" + master.getId(), "thumb"));
        List<IndexImpl> list = list(getLess(master.getResolution()));
        list.add(new IndexImpl(master.getId(), master.getResolution(), Status.BINDING, 0));
        master.setImpls(list);
        return master;
    }

    public List<Path> image(FFmpegProbeResult probe, String id, String type) throws IOException {
        Path thumbnail = tempFileService.tempFile("queue", id + (type.equals("jpeg") ? "_%d" : "") + "." + type);
        probe.getStreams().stream().filter(it -> it.codec_type == FFmpegStream.CodecType.VIDEO)
                .findFirst()
                .ifPresent(it -> {
                    long millis = ((long) it.duration) * 1000;
                    FFmpegOutputBuilder fFmpegOutputBuilder = new FFmpegBuilder()
                            .setInput(probe)
                            .addOutput(thumbnail.toString());
                    if (type.equals("webp")) {
                        fFmpegOutputBuilder.addExtraArgs("-ss", format(millis / 2)).addExtraArgs("-t", format(3 * 1000)).addExtraArgs("-loop", "0").setVideoFilter("select='gte(n\\,10)',scale=320:-1");
                    }
                    if (type.equals("jpeg")) {
                        fFmpegOutputBuilder.setVideoFilter("fps='(30/60)',select='gte(n\\,10)',scale=144:-1");
                    }
                    if (type.equals("png")) {
                        fFmpegOutputBuilder.setFrames(1).addExtraArgs("-ss", format(millis / 2)).setVideoFilter("select='gte(n\\,10)',scale=320:-1");
                    }
                    executor.createJob(fFmpegOutputBuilder.done()).run();
                });
        return Files.walk(thumbnail.getParent()).filter(it -> Files.isRegularFile(it)).collect(Collectors.toList());
    }

    public Path doConversion(FFmpegProbeResult probe, IndexImpl index) {
        Path out = tempFileService.tempFile("convert", index.getIndex() + UUID.randomUUID().toString().replace("-", ""));
        String[] split = index.getResolution().split("x");
        Integer width = Integer.valueOf(split[0]);
        Integer height = Integer.valueOf(split[1]);
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(probe)
                .addOutput(out.toString())
                .setFormat("mp4")
                .setVideoFilter("drawtext=\'text=\'" + logo + "\': fontsize=30 : fontcolor=white: x=((w/20)): y=((h/20))\'")
                .setVideoResolution(width, height)
                .done();
        FFmpegJob job = executor.createJob(builder);
        job.run();
        log.info("job " + job.getState());
        return out;
    }

    public Path hlsDoConversion(FFmpegProbeResult probe, Master master) {
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
                .addExtraArgs("-hls_time", "2")
                .addExtraArgs("-hls_list_size", "0")
                .addExtraArgs("-hls_enc", "1")
                .addExtraArgs("-hls_enc_key", master.getSecret())
                .addExtraArgs("-hls_enc_key_url", master.getId() + ".key")
                .addExtraArgs("-hls_enc_iv", UUID.randomUUID().toString().replace("-", ""))
                .addExtraArgs("-master_pl_name", master.getId() + ".m3u8")
                .done();
        FFmpegJob job = executor.createJob(builder);
        job.run();
        log.info("job " + job.getState());
        return out;
    }

    public Path split(FFmpegProbeResult probe, Master master) {
        Path out = tempFileService.tempFile("split", master.getId() + File.separator + master.getId() + File.separator + master.getId() + "_%d");
        tempFileService.mkdir(out.getParent().getParent());
        tempFileService.mkdir(out.getParent());

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(probe)
                .addOutput(out.toString())
                .addExtraArgs("-f", "segment")
                .addExtraArgs("-codec:", "copy")
                .addExtraArgs("-segment_time", "600")
                .addExtraArgs("-map", "0")
                .done();
        FFmpegJob job = executor.createJob(builder);
        job.run();
        log.info("job " + job.getState());
        return out.getParent();
    }
}
