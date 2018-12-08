package org.revo.Domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum Resolution {
    R4320("7680X4320", 7680, 4320),
    R2160("3840X2160", 3840, 2160),
    R1440("2560X1440", 2560, 1440),
    R1080("1920X1080", 1920, 1080),
    R720("1280X720", 1280, 720),
    R480("854X480", 854, 480),
    R360("640X360", 640, 360),
    R240("426X240", 426, 240),
    R144("256X144", 256, 144);

    Resolution(String resolution, int width, int height) {
        this.resolution = resolution;
        this.width = width;
        this.height = height;
    }

    private String resolution;
    private int width;
    private int height;

    public static List<Resolution> getLess(String resolution) {
        String[] split = resolution.split("X");
        Integer width = Integer.valueOf(split[0]);
        Integer height = Integer.valueOf(split[1]);
        return Arrays.asList(Resolution.values()).stream().filter(it -> it.getWidth() < width && it.getHeight() < height).collect(Collectors.toList());
    }
}
