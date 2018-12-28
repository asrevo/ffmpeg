package org.revo.Service;

import org.revo.Domain.Index;
import org.revo.Domain.Master;

import java.io.IOException;

public interface FfmpegService {

    Master convert(Master master) throws IOException;

    Index hls(Master master) throws IOException;

    Master queue(Master master) throws IOException;

    Master split(Master master) throws IOException;
}
