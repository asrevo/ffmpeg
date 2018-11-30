package org.revo.Service;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class Receiver {
    @Autowired
    private FfmpegService ffmpegService;
    @Autowired
    private TempFileService tempFileService;
    @Autowired
    private Processor processor;
    @Value("${spring.cloud.stream.rabbit.bindings.ffmpeg_converter_pop.consumer.max-priority}")
    private Integer maxPriority;

    @StreamListener(value = Processor.ffmpeg_converter_pop)
    @SendTo(value = Processor.bento4_hls)
    public Master convert(Message<Master> master) throws IOException {
        tempFileService.clear("convert");
        log.info("receive ffmpeg_converter_pop " + master.getPayload().getId());
        Master convert = ffmpegService.convert(master.getPayload());
        log.info("send bento4_hls " + convert.getId());
        return convert;
    }

    @StreamListener(value = Processor.ffmpeg_queue)
    @SendTo(value = Processor.tube_info)
    public Master queue(Message<Master> master) throws IOException {
        tempFileService.clear("queue");
        log.info("receive ffmpeg_queue " + master.getPayload().getId());
        Master queue = ffmpegService.queue(master.getPayload());
        List<IndexImpl> impls = queue.getImpls();
        if (queue.getFormat().equalsIgnoreCase("QuickTime / MOV")) {
            queue.setImpls(impls.stream().limit(1).collect(Collectors.toList()));
            log.info("send bento4_hls " + queue.getId());
            processor.bento4_hls().send(MessageBuilder.withPayload(queue).build());
        } else {
            queue.setImpls(impls.stream().limit(1).collect(Collectors.toList()));
            log.info("send ffmpeg_converter_push " + queue.getId());
            processor.ffmpeg_converter_push().send(MessageBuilder.withPayload(queue).setHeader("priority", maxPriority).build());
        }
        for (int i = 0; i < impls.stream().skip(1).collect(Collectors.toList()).size(); i++) {
            queue.setImpls(Collections.singletonList(impls.get(i + 1)));
            log.info("send ffmpeg_converter_push " + queue.getId());
            processor.ffmpeg_converter_push().send(MessageBuilder.withPayload(queue).setHeader("priority", maxPriority - 5 - i).build());
        }
        log.info("send tube_info " + queue.getId());
        queue.setImpls(impls);
        return queue;
    }
}