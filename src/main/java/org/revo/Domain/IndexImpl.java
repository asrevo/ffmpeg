package org.revo.Domain;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class IndexImpl {
    private String index;
    private String resolution;
    private Status status = Status.BINDING;
    private long execution = 0;

    public IndexImpl() {
    }

    public IndexImpl(String index, String resolution) {
        this.index = index;
        this.resolution = resolution;
    }

    public static List<IndexImpl> list(List<Resolution> less) {
        return less.stream().map(it -> new IndexImpl(new ObjectId().toString(), it.getResolution())).collect(Collectors.toList());
    }
}
