package org.revo.Service.Impl;

import com.comcast.viper.hlsparserj.tags.UnparsedTag;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.revo.Config.Env;
import org.revo.Domain.Index;
import org.revo.Domain.IndexImpl;
import org.revo.Domain.Master;
import org.revo.Service.FfmpegService;
import org.revo.Service.S3Service;
import org.revo.Service.SignedUrlService;
import org.revo.Util.FfmpegUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import static org.revo.Util.Utils.getMasterTag;
import static org.revo.Util.Utils.read;

@Service
public class FfmpegServiceImpl implements FfmpegService {
    @Autowired
    private S3Service s3Service;
    @Autowired
    private FFprobe fFprobe;
    @Autowired
    private SignedUrlService signedUrlService;
    @Autowired
    private Env env;
    @Autowired
    private FfmpegUtils ffmpegUtils;

    @Override
    public Master convert(Master master) throws IOException {
        long start = System.currentTimeMillis();
        IndexImpl index = master.getImpls().get(0);
        Path converted = ffmpegUtils.doConversion(fFprobe.probe(signedUrlService.generate(env.getBuckets().get("video"), master.getFile() + "/" + master.getId() + "/" + master.getId())), index);
        index.setExecution(System.currentTimeMillis() - start);
        s3Service.pushMediaDelete(master.getFile() + "/" + master.getId() + "/" + index.getIndex(), converted.toFile());
        master.setImpls(Collections.singletonList(index));
        return master;
    }

    @Override
    public Index hls(Master master) throws IOException {
        Path converted = ffmpegUtils.hlsDoConversion(fFprobe.probe(signedUrlService.generate(env.getBuckets().get("video"), master.getFile() + "/" + master.getId() + "/" + master.getImpls().get(0).getIndex())), master);
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

    @Override
    public Master queue(Master master) throws IOException {
        FFmpegProbeResult probe = fFprobe.probe(signedUrlService.generate(env.getBuckets().get("video"), master.getFile() + "/" + master.getId() + "/" + master.getId()));
        for (Path png : ffmpegUtils.image(probe, master.getId(), "png")) {
            File file = png.toFile();
            s3Service.pushImageDelete(png.getFileName().toString(), file);
        }
/*
        for (Path jpeg : ffmpegUtils.image(probe, master.getId(), "jpeg")) {
            File file = jpeg.toFile();
            s3Service.pushImageDelete(master.getId() + "/" + jpeg.getFileName().toString(), file);
        }
*/
        for (Path png : ffmpegUtils.image(probe, master.getId(), "webp")) {
            File file = png.toFile();
            s3Service.pushImageDelete(png.getFileName().toString(), file);
        }
        return ffmpegUtils.info(probe, master);
    }

}
