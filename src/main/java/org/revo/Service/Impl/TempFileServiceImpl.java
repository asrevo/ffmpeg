package org.revo.Service.Impl;

import org.apache.commons.io.FileUtils;
import org.revo.Service.TempFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class TempFileServiceImpl implements TempFileService {
    @Value("${spring.application.name}")
    private String project;
    private Path start = Paths.get(System.getProperty("java.io.tmpdir"), "asrevo");

    private Path mkdir(Path path) {
        if (!path.toFile().exists()) {
            path.toFile().mkdir();
        }
        return path;
    }

    public Path tempDir(String fun) throws IOException {
        return Files.createTempDirectory(mkdir(mkdir(mkdir(start).resolve(project)).resolve(fun)), "");
    }

    public Path tempFile(String fun) throws IOException {
        return Files.createTempFile(mkdir(mkdir(mkdir(start).resolve(project)).resolve(fun)), "", "");
    }

    @Override
    public Path tempFile(String fun, String name) throws IOException {
        return Paths.get(mkdir(mkdir(mkdir(start).resolve(project)).resolve(fun)).toString(), name);
    }

    public void clear(String fun) throws IOException {
        FileUtils.deleteDirectory(mkdir(mkdir(mkdir(start).resolve(project)).resolve(fun)).toFile());
    }
}
