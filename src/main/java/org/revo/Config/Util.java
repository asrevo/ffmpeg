package org.revo.Config;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

import static org.revo.Ffmpeg.FfmpegCLI.init;

/**
 * Created by ashraf on 23/04/17.
 */
@Configuration
public class Util {
    @Bean
    public FFmpegExecutor executor() {
        try {
//            init();
            FFprobe ffprobe = new FFprobe(System.getProperty("user.home") + File.separator + "ffmpeg" + File.separator + "bin" + File.separator + "ffprobe");
            FFmpeg ffmpeg = new FFmpeg(System.getProperty("user.home") + File.separator + "ffmpeg" + File.separator + "bin" + File.separator + "ffmpeg");
            return new FFmpegExecutor(ffmpeg, ffprobe);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Bean
    public FFprobe fFprobe(FFmpegExecutor executor) {
        try {
            return new FFprobe(System.getProperty("user.home") + File.separator + "ffmpeg" + File.separator + "bin" + File.separator + "ffprobe");
        } catch (IOException e) {
            return null;
        }

    }

}
