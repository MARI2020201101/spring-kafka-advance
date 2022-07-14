package com.mariworld.springkafkademo.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mariworld.springkafkademo.service.LibraryEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryEventConsumer {


    @Autowired
    private LibraryEventService libraryEventService;

    @KafkaListener(topics = {"demo-shyook-library-events"})
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        log.info("consumerRecord : {}", consumerRecord);
        libraryEventService.processLibraryEventV2(consumerRecord);
    }
}
