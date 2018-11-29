package org.revo.Service;

import org.revo.Domain.Master;

import java.io.IOException;

public interface FfmpegService {

    Master mp4(Master master) throws IOException;

    Master queue(Master master) throws IOException;
}
