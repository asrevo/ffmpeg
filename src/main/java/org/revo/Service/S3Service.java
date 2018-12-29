package org.revo.Service;

import org.revo.Domain.Master;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by ashraf on 15/04/17.
 */
public interface S3Service {

    Path pull(String fun, String key) throws IOException;

    void pushMediaDelete(String key, File file);

    void pushImageDelete(String key, File file);

    void deleteMedia(String key);

    void push(Path base, Path parent);

    void saveTsDelete(File path, String key);

    void saveVideoDelete(File path, String key);

    void pushSplitedVideo(Master master, Path videos) throws IOException;
}
