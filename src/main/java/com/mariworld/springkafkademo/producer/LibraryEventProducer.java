package com.mariworld.springkafkademo.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mariworld.springkafkademo.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class LibraryEventProducer {
    @Autowired
    private KafkaTemplate<Integer,String> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void setDefaultTopic(){
        kafkaTemplate.setDefaultTopic("demo-shyook-library-events");
    }

    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent.toString());
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);

        //비동기 스레드 & 논블로킹
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(ex);
            }
            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuceess(key,value,result);
            }
        });
    }
    public void sendLibraryEventV2(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent.toString());
        String topic = "demo-shyook-library-events";


        ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value, topic);
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);

        //비동기 스레드 & 논블로킹
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(ex);
            }
            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuceess(key,value,result);
            }
        });
    }



    public SendResult<Integer, String> sendLibrarySynchronous(LibraryEvent libraryEvent) throws JsonProcessingException {
        //동기 스레드 & 블로킹 (get 호출)
        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent.toString());
        SendResult<Integer, String> sendResult = null;
        try {
            sendResult = kafkaTemplate.sendDefault(key, value).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error Message Send ----> {} ", e.getMessage());
        }
        return sendResult;
    }

    private ProducerRecord<Integer,String> buildProducerRecord(Integer key, String value, String topic) {

        List<Header> recordHeaders = List.of(
                new RecordHeader("event-source", "scanner".getBytes(StandardCharsets.UTF_8)),
                new RecordHeader("custom-header", "hello-world".getBytes(StandardCharsets.UTF_8))
        );

        return new ProducerRecord<>(topic,null, key, value,recordHeaders);
    }
    private void handleFailure(Throwable ex) {
        log.error("Error Message Send ----> {}", ex.getMessage());
    }

    private void handleSuceess(Integer key, String value, SendResult<Integer, String> result) {
        log.warn("Message Send Successfully ----> key : {} , value : {} , partition : {}"
                , key,value,result.getRecordMetadata().partition());
        log.warn("result ==> {}", result);

    }
}
