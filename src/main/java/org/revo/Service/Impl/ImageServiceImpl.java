package org.revo.Service.Impl;

import net.coobird.thumbnailator.Thumbnails;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.jcodec.scale.AWTUtil;
import org.revo.Service.ImageService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class ImageServiceImpl implements ImageService {
    @Override
    public File convertImage(File video) {
        try {
            int frame = MP4Demuxer.createMP4Demuxer(NIOUtils.readableChannel(video)).getVideoTrack().getMeta().getTotalFrames() / 2;
            Picture picture = FrameGrab.getFrameFromFile(video, frame);
            BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
            String type = "png";
            File output = Files.createTempFile("", "").toFile();
            ImageIO.write(bufferedImage, type, output);
            return output;
        } catch (IOException | JCodecException ignored) {
            return null;
        }
    }

    @Override
    public File resize(File fullImage, int width, int height) {
        try {
            BufferedImage bufferedImage = Thumbnails.of(ImageIO.read(fullImage)).size(width, height).asBufferedImage();
            String type = "png";
            File output = Files.createTempFile("", "_" + width + "X" + height).toFile();
            ImageIO.write(bufferedImage, type, output);
            return output;
        } catch (IOException e) {

        }
        return null;
    }

}
