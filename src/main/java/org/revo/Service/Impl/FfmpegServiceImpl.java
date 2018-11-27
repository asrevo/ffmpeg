package org.revo.Service.Impl;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.revo.Domain.Base;
import org.revo.Domain.Index;
import org.revo.Domain.IndexImpl;
import org.revo.Domain.Master;
import org.revo.Service.FfmpegService;
import org.revo.Service.ImageService;
import org.revo.Service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FfmpegServiceImpl implements FfmpegService {
    @Autowired
    private ImageService imageService;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private FFmpegExecutor executor;
    @Autowired
    private FFprobe fFprobe;

    @Override
    public Master mp4(Master payload) throws IOException {
        Path source = s3Service.pull(payload.getId());
        Path converted = convert(source, payload.getImpls().get(0));
        s3Service.pushMedia(payload.getImpls().get(0).getIndex(), converted.toFile());
        converted.toFile().delete();
        source.toFile().delete();
        return payload;
    }

    @Override
    public void png(Base base) throws IOException {
        Path source = s3Service.pull(base.getId());
        File fullImage = imageService.convertImage(source.toFile());
        if (source != null) {
            s3Service.pushImage(base.getId() + ".png", fullImage);
            File resize = imageService.resize(fullImage, 400, 320);
            s3Service.pushImage(base.getId() + "_" + 400 + "X" + 320 + ".png", resize);
            fullImage.delete();
            resize.delete();
        }
        source.toFile().delete();
    }

    private Path convert(Path in, IndexImpl index) throws IOException {
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
}
