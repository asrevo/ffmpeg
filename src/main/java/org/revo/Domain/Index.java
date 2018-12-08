package org.revo.Domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Index {
    private String id;
    private String master;
    private String stream;
    private String average_bandwidth;
    private String bandwidth;
    private String codecs;
    private String resolution;
    private long execution;
}