package org.revo.Service;

import org.revo.Domain.Base;
import org.revo.Domain.Master;

import java.io.IOException;

public interface FfmpegService {

    Master mp4(Master master) throws IOException;

    void png(Base base) throws IOException;
}
