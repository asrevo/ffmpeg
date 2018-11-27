package org.revo.Ffmpeg;


import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.springframework.util.FileSystemUtils.deleteRecursively;


/**
 * Created by ashraf on 14/04/17.
 */
public class FfmpegCLI {

    public static void init() {
        try {
            String Ffmpeg = System.getProperty("user.home") + File.separator + "ffmpeg";
            String Ffprobe = System.getProperty("user.home") + File.separator + "ffprobe";
            File path = new File(Ffmpeg);
            File bin = new File(Ffmpeg + "/bin/");
            deleteRecursively(path);
            path.mkdir();
            bin.mkdir();
            FileUtils.copyURLToFile(new URL("https://raw.githubusercontent.com/asrevo/pre-build/master/ffmpeg.zip"), new File(Ffmpeg + ".zip"));
            FileUtils.copyURLToFile(new URL("https://raw.githubusercontent.com/asrevo/pre-build/master/ffprobe.zip"), new File(Ffprobe + ".zip"));
            new ZipFile(Ffmpeg + ".zip").extractAll(bin.getPath());
            new ZipFile(Ffprobe + ".zip").extractAll(bin.getPath());
            String[] CmdChmod = {"chmod", "-R", "+xr", bin.getPath()};
            new ProcessBuilder(CmdChmod).start();
        } catch (IOException | ZipException ignored) {
        }
    }
}
