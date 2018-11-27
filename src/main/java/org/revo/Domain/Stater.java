package org.revo.Domain;

import java.util.Date;

public class Stater {
    private String id;
    private Queue queue;
    private State state;
    private Base base;
    private Date createdDate;

    public Stater() {
    }

    public Stater(Base base, Queue queue, State state) {
        this.base = base;
        this.queue = queue;
        this.state = state;
        this.createdDate = new Date();
    }
}