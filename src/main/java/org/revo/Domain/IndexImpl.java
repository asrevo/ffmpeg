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
    private Status status;
    private long execution;

    public IndexImpl() {
    }

    public IndexImpl(String index, String resolution, Status status, long execution) {
        this.index = index;
        this.resolution = resolution;
        this.status = status;
        this.execution = execution;
    }

    public static List<IndexImpl> list(List<Resolution> less) {
        return less.stream().map(it -> new IndexImpl(new ObjectId().toString(), it.getResolution(), Status.BINDING, 0)).collect(Collectors.toList());
    }
}
