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

import static org.springframework.messaging.support.MessageBuilder.withPayload;

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
            log.info("receive ffmpeg_converter_pop " + master.getPayload().getId() + " and " + master.getPayload().getImpls().stream().map(IndexImpl::getResolution).collect(Collectors.joining(",")));
            Master convert = ffmpegService.convert(master.getPayload());
            log.info("send ffmpeg_hls " + convert.getId());
            processor.ffmpeg_hls_push().send(MessageBuilder.withPayload(convert).build());
        } catch (IOException e) {
            log.info("convert error " + e.getMessage());
        }
    }

    @StreamListener(value = Processor.ffmpeg_hls_pop)
    public void hls(Message<Master> base) throws IOException {
        tempFileService.clear("hls");
        log.info("receive ffmpeg_hls " + base.getPayload().getId());
        Index index = ffmpegService.hls(base.getPayload());
        log.info("send tube_hls " + index.getId());
        processor.tube_hls().send(withPayload(index).build());
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
            if (queue.isMp4()) {
                queue.setImpls(impls.stream().limit(1).collect(Collectors.toList()));
                log.info("send ffmpeg_hls " + queue.getId() + " and " + queue.getImpls().stream().map(IndexImpl::getResolution).collect(Collectors.joining(",")));
                processor.ffmpeg_hls_push().send(MessageBuilder.withPayload(queue).build());
                processor.ffmpeg_converter_push().send(MessageBuilder.withPayload(queue).setHeader("priority", maxPriority - 5).build());
            } else {
                queue.setImpls(impls.stream().limit(1).collect(Collectors.toList()));
                log.info("send ffmpeg_converter_push " + queue.getId() + " and " + queue.getImpls().stream().map(IndexImpl::getResolution).collect(Collectors.joining(",")));
                processor.ffmpeg_converter_push().send(MessageBuilder.withPayload(queue).setHeader("priority", maxPriority).build());
            }
            for (int i = 0; i < impls.stream().skip(1).collect(Collectors.toList()).size(); i++) {
                queue.setImpls(Collections.singletonList(impls.get(i + 1)));
                log.info("send ffmpeg_converter_push " + queue.getId());
                processor.ffmpeg_converter_push().send(MessageBuilder.withPayload(queue).setHeader("priority", maxPriority - 5 - i).build());
            }
        } catch (IOException e) {
            log.info("queue error " + e.getMessage());
        }
    }
}