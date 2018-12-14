package org.revo.Config;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface Processor {
    String ffmpeg_queue = "ffmpeg_queue";

    @Input("ffmpeg_queue")
    SubscribableChannel ffmpeg_queue();

    String ffmpeg_hls_pop = "ffmpeg_hls_pop";

    @Input("ffmpeg_hls_pop")
    SubscribableChannel ffmpeg_hls_pop();

    String ffmpeg_converter_pop = "ffmpeg_converter_pop";

    @Input("ffmpeg_converter_pop")
    SubscribableChannel ffmpeg_converter_pop();

    String ffmpeg_converter_push = "ffmpeg_converter_push";

    @Output("ffmpeg_converter_push")
    MessageChannel ffmpeg_converter_push();

    String ffmpeg_hls_push = "ffmpeg_hls_push";

    @Output("ffmpeg_hls_push")
    MessageChannel ffmpeg_hls_push();

    String tube_info = "tube_info";

    @Output("tube_info")
    MessageChannel tube_info();

    String tube_hls = "tube_hls";

    @Output("tube_hls")
    MessageChannel tube_hls();

    String feedback_queue = "feedback_queue";

    @Output("feedback_queue")
    MessageChannel feedback_queue();
}
