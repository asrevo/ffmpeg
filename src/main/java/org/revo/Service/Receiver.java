package org.revo.Service;

import lombok.extern.slf4j.Slf4j;
import org.revo.Config.Processor;
import org.revo.Domain.Master;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.integration.annotation.MessageEndpoint;
import reactor.core.publisher.Flux;

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

    @StreamListener()
    public void convert(@Input(Processor.ffmpeg_converter_pop) Flux<Master> master) {
        master.doOnNext(it -> {
            log.info("receive ffmpeg_converter_pop " + it.getId());
        }).subscribe();

/*
        try {
            tempFileService.clear("convert");
            tempFileService.clear("hls");
            log.info("receive ffmpeg_converter_pop " + master.getPayload().getId() + " and " + master.getPayload().getImpls().stream().map(IndexImpl::getResolution).collect(Collectors.joining(",")));
            Index index = ffmpegService.hls(master.getPayload().getId().equals(master.getPayload().getImpls().get(0).getIndex()) ? master.getPayload() : ffmpegService.convert(master.getPayload()));
            log.info("send tube_hls " + index.getId());
            processor.tube_hls().send(MessageBuilder.withPayload(index).build());
        } catch (IOException e) {
            log.info("convert error " + e.getMessage());
        } finally {
            tempFileService.clear("convert");
            tempFileService.clear("hls");
        }
*/
    }

    @StreamListener()
    public void queue(@Input(Processor.ffmpeg_queue) Flux<Master> master) {
        master.doOnNext(it -> {
            log.info("receive ffmpeg_queue " + it.getId());
        }).subscribe();

/*
        try {
            tempFileService.clear("queue");
            log.info("receive ffmpeg_queue " + master.getPayload().getId());
            log.info("will split");
            Master queue = ffmpegService.image(ffmpegService.queue(ffmpegService.split(master.getPayload())));
            log.info("send tube_info " + queue.getId());
            processor.tube_info().send(MessageBuilder.withPayload(queue).build());
            queue.getImpls().stream().sorted((o1, o2) -> isLess(o1.getResolution(), o2.getResolution())).forEach(it -> {
                queue.setImpls(singletonList(it));
                processor.ffmpeg_converter_push().send(MessageBuilder.withPayload(queue).setHeader("priority", findOne(it.getResolution()).map(p -> maxPriority - p).orElse(maxPriority)).build());
            });
        } catch (IOException e) {
            log.info("queue error " + e.getMessage());
        } finally {
            tempFileService.clear("queue");
            tempFileService.clear("split");
            tempFileService.clear("hls");
        }
*/
    }
}