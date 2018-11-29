package org.revo.Service;

import org.revo.Config.Processor;
import org.revo.Domain.IndexImpl;
import org.revo.Domain.Master;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ashraf on 23/04/17.
 */
@MessageEndpoint
public class Receiver {
    @Autowired
    private FfmpegService ffmpegService;
    @Autowired
    private Processor processor;
    @Value("${spring.cloud.stream.rabbit.bindings.ffmpeg_converter_pop.consumer.max-priority}")
    private Integer maxPriority;

    @StreamListener(value = Processor.ffmpeg_converter_pop)
    @SendTo(value = Processor.bento4_hls)
    public Master convert(Message<Master> master) throws IOException {
        return ffmpegService.mp4(master.getPayload());
    }

    @StreamListener(value = Processor.ffmpeg_queue)
    @SendTo(value = Processor.tube_info)
    public Master queue(Message<Master> master) throws IOException {
        Master queue = ffmpegService.queue(master.getPayload());
        List<IndexImpl> impls = queue.getImpls();
        if (queue.getFormat().equalsIgnoreCase("QuickTime / MOV")) {
            queue.setImpls(impls.stream().limit(1).collect(Collectors.toList()));
            processor.bento4_hls().send(MessageBuilder.withPayload(queue).build());
        } else {
            queue.setImpls(impls.stream().limit(1).collect(Collectors.toList()));
            processor.ffmpeg_converter_push().send(MessageBuilder.withPayload(queue).setHeader("priority", maxPriority).build());
        }
        for (int i = 0; i < impls.stream().skip(1).collect(Collectors.toList()).size(); i++) {
            queue.setImpls(Collections.singletonList(impls.get(i + 1)));
            processor.ffmpeg_converter_push().send(MessageBuilder.withPayload(queue).setHeader("priority", maxPriority - 5 - i).build());
        }
        return queue;
    }
}
//        processor.ToFeedBack_push().send(withPayload(new Stater(media.getPayload(), Queue.FFMPEG_MP4, State.ON_GOING)).build());
//        processor.ToFeedBack_push().send(withPayload(new Stater(media.getPayload(), Queue.BENTO4_HLS, State.QUEUED)).build());
//        processor.ToFeedBack_push().send(withPayload(new Stater(media.getPayload(), Queue.FFMPEG_MP4, State.UNDER_GOING)).build());
//        processor.ToFeedBack_push().send(withPayload(new Stater(media.getPayload(), Queue.FFMPEG_PNG, State.ON_GOING)).build());
//send  queue.getImpls().get(0) to bento4
//            send to queue.getImpls().get(0) to mp4 with 20 priority
//            send to queue.getImpls().get(i>0) to mp4 with less 15 priority
//        processor.ToFeedBack_push().send(withPayload(new Stater(media.getPayload(), Queue.FFMPEG_PNG, State.UNDER_GOING)).build());
