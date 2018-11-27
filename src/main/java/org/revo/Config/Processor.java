package org.revo.Config;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface Processor {
    String ToFfmpeg_mp4 = "ToFfmpeg_mp4";

    @Input("ToFfmpeg_mp4")
    MessageChannel ToFfmpeg_mp4();

    String ToFfmpeg_png = "ToFfmpeg_png";

    @Input("ToFfmpeg_png")
    MessageChannel ToFfmpeg_png();

    String ToBento4_hls = "ToBento4_hls";

    @Output("ToBento4_hls")
    SubscribableChannel ToBento4_hls();

    String ToFeedBack_push = "ToFeedBack_push";

    @Output("ToFeedBack_push")
    MessageChannel ToFeedBack_push();
}
