package org.revo.Domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Base {
    private String id;
    private String title;
    private String meta;
    private String userId;
    private Date createdDate = new Date();
}