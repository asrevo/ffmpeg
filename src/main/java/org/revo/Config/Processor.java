package org.revo.Config;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface Processor {
    String ffmpeg_queue = "ffmpeg_queue";

    @Input("ffmpeg_queue")
    SubscribableChannel ffmpeg_queue();

    String ffmpeg_converter_pop = "ffmpeg_converter_pop";

    @Input("ffmpeg_converter_pop")
    SubscribableChannel ffmpeg_converter_pop();

    String ffmpeg_converter_push = "ffmpeg_converter_push";

    @Output("ffmpeg_converter_push")
    MessageChannel ffmpeg_converter_push();

    String bento4_hls = "bento4_hls";

    @Output("bento4_hls")
    MessageChannel bento4_hls();

    String tube_info = "tube_info";

    @Output("tube_info")
    MessageChannel tube_info();

    String feedBack_push = "feedBack_push";

    @Output("feedBack_push")
    MessageChannel feedBack_push();
}
