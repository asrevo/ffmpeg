package org.revo.Domain;

import com.comcast.viper.hlsparserj.tags.UnparsedTag;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Index {
    private String id;
    private String master;
    private String stream;
    private List<UnparsedTag> tags;
    private String average_bandwidth;
    private String bandwidth;
    private String codecs;
    private String resolution;
    private long execution;
}