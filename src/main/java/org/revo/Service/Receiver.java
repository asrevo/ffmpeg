package org.revo.Service;

import lombok.extern.slf4j.Slf4j;
import org.revo.Config.Processor;
import org.revo.Domain.Index;
import org.revo.Domain.IndexImpl;
import org.revo.Domain.Master;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.messaging.Message;
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
    public void convert(Message<Master> master) {
        try {
            tempFileService.clear("convert");
            tempFileService.clear("hls");
            log.info("receive ffmpeg_converter_pop " + master.getPayload().getId() + " and " + master.getPayload().getImpls().stream().map(IndexImpl::getResolution).collect(Collectors.joining(",")));
            Index index = ffmpegService.hls(ffmpegService.convert(master.getPayload()));
            log.info("send tube_hls " + index.getId());
            processor.tube_hls().send(MessageBuilder.withPayload(index).build());
        } catch (IOException e) {
            log.info("convert error " + e.getMessage());
        } finally {
            tempFileService.clear("convert");
            tempFileService.clear("hls");
        }
    }

    @StreamListener(value = Processor.ffmpeg_queue)
    public void queue(Message<Master> master) {
        try {
            tempFileService.clear("queue");
            log.info("receive ffmpeg_queue " + master.getPayload().getId());
            Master queue = ffmpegService.queue(master.getPayload());
            log.info("send tube_info " + queue.getId());
            processor.tube_info().send(MessageBuilder.withPayload(queue).build());
            List<IndexImpl> impls = queue.getImpls();
            for (int i = 0; i < impls.size(); i++) {
                log.info("send ffmpeg_converter_push " + queue.getId());
                queue.setImpls(Collections.singletonList(impls.get(i)));
                processor.ffmpeg_converter_push().send(MessageBuilder.withPayload(queue).setHeader("priority", (queue.isMp4() && i != 0) ? (maxPriority - 5 - i) : (maxPriority)).build());
            }
        } catch (IOException e) {
            log.info("queue error " + e.getMessage());
        } finally {
            tempFileService.clear("queue");
        }
    }
}