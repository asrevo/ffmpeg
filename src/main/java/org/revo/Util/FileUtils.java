package org.revo.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class FileUtils {
    public static String read(Path path) {
        try {
            return Files.lines(path).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "";
        }
    }

}
