package org.revo.Service.Impl;

import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.revo.Config.Env;
import org.revo.Domain.IndexImpl;
import org.revo.Domain.Master;
import org.revo.Domain.Status;
import org.revo.Service.FfmpegService;
import org.revo.Service.S3Service;
import org.revo.Service.SignedUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

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
    private Env env;


    @Override
    public Master convert(Master payload) throws IOException {
        Path source = s3Service.pull(payload.getId());
        log.info(source.toFile().toString()+ "        "+source.toFile().length()+"        "+source.toFile().getFreeSpace()+"         ");
        Path converted = doConversion(source, payload.getImpls().get(0));
        s3Service.pushMedia(payload.getImpls().get(0).getIndex(), converted.toFile());
        converted.toFile().delete();
        source.toFile().delete();
        return payload;
    }

    @Override
    public Master queue(Master master) throws IOException {
        FFmpegProbeResult probe = fFprobe.probe(signedUrlService.generate(env.getBuckets().get("video"), master.getId()));
        File image = image(probe);
        s3Service.pushImage(master.getId() + ".png", image);
        image.delete();
        return info(probe, master);
    }

    private Master info(FFmpegProbeResult probe, Master master) {
        master.setResolution(probe.getStreams().stream().filter(it -> it.codec_type == FFmpegStream.CodecType.VIDEO).map(it -> it.width + "X" + it.height).collect(Collectors.joining()));
        master.setTime(probe.getFormat().duration);
        master.setFormat(probe.getFormat().format_long_name);
        master.setStream("#EXTM3U\n#EXT-X-VERSION:4\n# Media Playlists\n");
        master.setImage(signedUrlService.getUrl(master.getId() + ".png", "thumb"));
        List<IndexImpl> list = list(getLess(master.getResolution()));
        list.add(0, new IndexImpl(master.getId(), master.getResolution(), Status.BINDING));
        master.setImpls(list);
        return master;
    }

    private File image(FFmpegProbeResult probe) throws IOException {
        Path thumbnail = Files.createTempFile("thumbnail", ".png");
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(probe)
                .addOutput(thumbnail.toString())
                .setFrames(1)
                .setVideoFilter("select='gte(n\\,10)',scale=320:-1").done();
        executor.createJob(builder, progress -> {
        }).run();
        return thumbnail.toFile();
    }

    private Path doConversion(Path in, IndexImpl index) throws IOException {
        Path out = in.getParent().resolve(index.getIndex());
        Integer width = Integer.valueOf(index.getResolution().split("X")[0]);
        Integer height = Integer.valueOf(index.getResolution().split("X")[1]);
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(fFprobe.probe(in.toString()))
                .addOutput(out.toString())
                .setFormat("mp4")
                .setVideoResolution(width, height)
                .done();
        executor.createJob(builder, progress -> {
        }).run();
        return out;
    }

    public static void main(String[] args) {
        FfmpegServiceImpl ffmpegService = new FfmpegServiceImpl();
        try {
            ffmpegService.fFprobe = new FFprobe(System.getProperty("user.home") + File.separator + "ffmpeg" + File.separator + "bin" + File.separator + "ffprobe");
            FFmpeg ffmpeg = new FFmpeg(System.getProperty("user.home") + File.separator + "ffmpeg" + File.separator + "bin" + File.separator + "ffmpeg");
            ffmpegService.executor = new FFmpegExecutor(ffmpeg, ffmpegService.fFprobe);

            Path out = ffmpegService.doConversion(Paths.get("/home/ashraf/ffmpeg/bin/a.mkv"), new IndexImpl("5bfd3df1ad8ce6617f9bf635", "1280X720", Status.BINDING));
            System.out.println(out);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
