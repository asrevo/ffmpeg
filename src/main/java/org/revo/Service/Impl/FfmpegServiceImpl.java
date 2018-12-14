package org.revo.Service.Impl;

import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.revo.Config.Env;
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
import java.nio.file.Path;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.Comparator.comparingInt;
import static org.revo.Domain.IndexImpl.list;
import static org.revo.Domain.Resolution.getLess;

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

    @Override
    public Master queue(Master master) throws IOException {
        FFmpegProbeResult probe = fFprobe.probe(signedUrlService.generate(env.getBuckets().get("video"), master.getId()));
        File image = image(probe, master.getId());
        s3Service.pushImage(master.getId() + ".png", image);
        log.info("image " + image.toString());
        return info(probe, master);
    }

    private Master info(FFmpegProbeResult probe, Master master) {
        master.setResolution(probe.getStreams().stream().filter(it -> it.codec_type == FFmpegStream.CodecType.VIDEO).max(comparingInt(o -> o.height * o.width)).
                map(it -> ((it.width / 2) * 2) + "x" + ((it.height / 2) * 2)).orElse(""));
        master.setTime(probe.getFormat().duration);
        master.setMp4(probe.getFormat().format_long_name.equalsIgnoreCase("QuickTime / MOV"));
        master.setImage(signedUrlService.getUrl(master.getId() + ".webp", "thumb"));
        master.setImpls(list(getLess(master.getResolution())));
        return master;
    }

    private File image(FFmpegProbeResult probe, String id) throws IOException {
        Path thumbnail = tempFileService.tempFile("queue", id + ".webp");
        probe.getStreams().stream().filter(it -> it.codec_type == FFmpegStream.CodecType.VIDEO).forEach(it -> {
            long millis = ((long) it.duration) * 1000;
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(probe)
                    .addOutput(thumbnail.toString())
                    .addExtraArgs("-ss", format(millis / 2))
                    .addExtraArgs("-t", format(3 * 1000))
                    .setVideoFilter("select='gte(n\\,10)',scale=320:-1").done();
            executor.createJob(builder).run();
        });
        return thumbnail.toFile();
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
