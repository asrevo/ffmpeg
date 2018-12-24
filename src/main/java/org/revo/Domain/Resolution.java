package org.revo.Domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public enum Resolution {
    R4320("7680x4320", 7680, 4320, 8),
    R2160("3840x2160", 3840, 2160, 7),
    R1440("2560x1440", 2560, 1440, 6),
    R1080("1920x1080", 1920, 1080, 5),
    R720("1280x720", 1280, 720, 2),
    R480("854x480", 854, 480, 3),
    R360("640x360", 640, 360, 1),
    R240("426x240", 426, 240, 4),
    R144("256x144", 256, 144, 0);

    Resolution(String resolution, int width, int height, int priority) {

        this.resolution = resolution;
        this.width = width;
        this.height = height;
        this.priority = priority;
    }

    private String resolution;
    private int width;
    private int height;
    private int priority;

    public static List<Resolution> getLess(String resolution) {
        return Arrays.asList(Resolution.values()).stream().filter(it -> getValue(resolution) - getValue(it.resolution) >= 0).collect(Collectors.toList());
    }

    private static double getValue(String resolution) {
        String[] split = resolution.split("x");
        return Math.pow(Math.pow(Integer.valueOf(split[0]), 2) + Math.pow(Integer.valueOf(split[1]), 2), .5);
    }

    public static int isLess(String resolution1, String resolution2) {
        return Double.compare(getValue(resolution1), getValue(resolution2));
    }

    public static Optional<Integer> findOne(String resolution) {
        return Arrays.asList(Resolution.values()).stream().filter(it -> it.resolution.equalsIgnoreCase(resolution)).findAny().map(it -> it.priority);
    }
}
