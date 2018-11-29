package org.revo.Domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum Resolution {
    R2160("3840X2160", 2160),
    R1440("2560X1440", 1440),
    R1080("1920X1080", 1080),
    R720("1280X720", 720),
    R480("854X480", 480),
    R360("640X360", 360),
    R240("426X240", 240);

    Resolution(String resolution, int value) {
        this.resolution = resolution;
        this.value = value;
    }

    private String resolution;
    private int value;

    public static List<Resolution> getLess(String resolution) {
        Integer v = Integer.valueOf(resolution.split("X")[1]);
        return Arrays.asList(Resolution.values()).stream().filter(it -> it.getValue() < v).collect(Collectors.toList());
    }
}
