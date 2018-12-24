package org.revo.Domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum Resolution {
    R4320("7680x4320", 7680, 4320),
    R2160("3840x2160", 3840, 2160),
    R1440("2560x1440", 2560, 1440),
    R1080("1920x1080", 1920, 1080),
    R720("1280x720", 1280, 720),
    R480("854x480", 854, 480),
    R360("640x360", 640, 360),
    R240("426x240", 426, 240),
    R144("256x144", 256, 144);

    Resolution(String resolution, int width, int height) {
        this.resolution = resolution;
        this.width = width;
        this.height = height;
    }

    private String resolution;
    private int width;
    private int height;

    public static List<Resolution> getLess(String resolution) {
        return Arrays.asList(Resolution.values()).stream().filter(it -> getValue(resolution) - getValue(it.resolution) >= 0).collect(Collectors.toList());
    }

    private static double getValue(String resolution) {
        String[] split = resolution.split("x");
        return Math.pow(Math.pow(Integer.valueOf(split[0]), 2) + Math.pow(Integer.valueOf(split[1]), 2), .5);
    }

    public static int isLess(String resolution1, String resolution2) {
        return isLess(Resolution.valueOf(resolution1), Resolution.valueOf(resolution2));
    }

    public static int isLess(Resolution r1, Resolution r2) {
        return Comparator.comparing((Function<Resolution, Integer>) o -> o.getWidth() * o.getHeight()).compare(r1, r2);
    }
}
