package org.revo.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by ashraf on 15/04/17.
 */
public interface S3Service {

    Path pull(String key) throws IOException;

    void pushMedia(String key, File file);

    void pushImage(String key, File file);

    void deleteMedia(String key);
}
