package org.revo.Service;

import org.revo.Domain.Master;

import java.io.IOException;

public interface FfmpegService {

    Master convert(Master master) throws IOException;

    Master queue(Master master) throws IOException;
}
