package com.mariworld.springkafkademo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mariworld.springkafkademo.domain.LibraryEvent;
import com.mariworld.springkafkademo.domain.LibraryEventType;
import com.mariworld.springkafkademo.producer.LibraryEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class LibraryEventController {
    @Autowired
    private LibraryEventProducer libraryEventProducer;

    @PostMapping("/v1/library-event")
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody LibraryEvent libraryEvent) throws JsonProcessingException {

        log.warn("--------------  before sending event --------------------");
//        libraryEventProducer.sendLibraryEvent(libraryEvent);
//        SendResult<Integer, String> sendResult = libraryEventProducer.sendLibrarySynchronous(libraryEvent);
//        log.warn("sendResult ==> {}", sendResult);

        libraryEvent.setLibraryEventType(LibraryEventType.NEW);
        libraryEventProducer.sendLibraryEventV2(libraryEvent);
        log.warn("--------------  after sending event --------------------");
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping("/v1/library-event")
    public ResponseEntity<LibraryEvent> putLibraryEvent(@RequestBody LibraryEvent libraryEvent) {
        return null;
    }
}
