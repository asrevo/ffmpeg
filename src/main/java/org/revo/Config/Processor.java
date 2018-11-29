package org.revo.Config;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface Processor {
    String ffmpeg_queue = "ffmpeg_queue";

    @Input("ffmpeg_queue")
    MessageChannel ffmpeg_queue();

    String ffmpeg_converter_pop = "ffmpeg_converter_pop";

    @Input("ffmpeg_converter_pop")
    MessageChannel ffmpeg_converter_pop();

    String ffmpeg_converter_push = "ffmpeg_converter_push";

    @Output("ffmpeg_converter_push")
    SubscribableChannel ffmpeg_converter_push();

    String bento4_hls = "bento4_hls";

    @Output("bento4_hls")
    SubscribableChannel bento4_hls();

    String tube_info = "tube_info";

    @Output("tube_info")
    SubscribableChannel tube_info();

    String feedBack_push = "feedBack_push";

    @Output("feedBack_push")
    MessageChannel feedBack_push();
}
