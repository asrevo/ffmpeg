package org.revo.Service;

import java.io.IOException;
import java.nio.file.Path;

public interface TempFileService {
    Path tempDir(String fun) throws IOException;

    Path tempFile(String fun) throws IOException;

    Path tempFile(String fun, String name) throws IOException;

    void clear(String fun) throws IOException;
}
