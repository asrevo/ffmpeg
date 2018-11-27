package org.revo.Service;

import java.io.File;

public interface ImageService {
    File convertImage(File video);

    File resize(File fullImage, int width, int height);

}
