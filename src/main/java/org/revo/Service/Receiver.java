package org.revo.Service;

import org.revo.Config.Processor;
import org.revo.Domain.Master;
import org.revo.Domain.Queue;
import org.revo.Domain.State;
import org.revo.Domain.Stater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;

import java.io.IOException;

import static org.springframework.integration.support.MessageBuilder.withPayload;

/**
 * Created by ashraf on 23/04/17.
 */
@MessageEndpoint
public class Receiver {
    @Autowired
    private FfmpegService ffmpegService;
    @Autowired
    private Processor processor;

    @StreamListener(value = Processor.ToFfmpeg_mp4)
    @SendTo(value = Processor.ToBento4_hls)
    public Master mp4(Message<Master> media) throws IOException {
//        processor.ToFeedBack_push().send(withPayload(new Stater(media.getPayload(), Queue.FFMPEG_MP4, State.ON_GOING)).build());
        Master process = ffmpegService.mp4(media.getPayload());
//        processor.ToFeedBack_push().send(withPayload(new Stater(media.getPayload(), Queue.BENTO4_HLS, State.QUEUED)).build());
//        processor.ToFeedBack_push().send(withPayload(new Stater(media.getPayload(), Queue.FFMPEG_MP4, State.UNDER_GOING)).build());
        return process;
    }

    @StreamListener(value = Processor.ToFfmpeg_png)
    public void png(Message<Master> media) throws IOException {
//        processor.ToFeedBack_push().send(withPayload(new Stater(media.getPayload(), Queue.FFMPEG_PNG, State.ON_GOING)).build());
        ffmpegService.png(media.getPayload());
//        processor.ToFeedBack_push().send(withPayload(new Stater(media.getPayload(), Queue.FFMPEG_PNG, State.UNDER_GOING)).build());
    }
}