package org.revo.Domain;

public enum Queue {
    FFMPEG("FFMPEG", 2),
    FILE("FILE", 3),
    TUBE_PUBLISH("TUBE_PUBLISH", 4),
    TUBE_QUEUE("TUBE_QUEUE", 5),
    TUBE_STORE("TUBE_STORE", 6), TUBE_HLS("TUBE_HLS", 0), TUBE_INFO("TUBE_INFO", 0), FFMPEG_MP4("FFMPEG_MP4", 0), FFMPEG_PNG("FFMPEG_PNG", 0);


    private String name;
    private int state;

    Queue(String name, int state) {
        this.name = name;
        this.state = state;
    }

}
