package com.mariworld.springkafkademo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mariworld.springkafkademo.entity.Book;
import com.mariworld.springkafkademo.entity.LibraryEvent;
import com.mariworld.springkafkademo.entity.LibraryEventType;
import com.mariworld.springkafkademo.jpa.LibraryEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.mariworld.springkafkademo.entity.LibraryEventType.NEW;
import static com.mariworld.springkafkademo.entity.LibraryEventType.UPDATE;

@Service
@Slf4j
public class LibraryEventService {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LibraryEventRepository libraryEventRepository;

    public void processLibraryEventV2(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);

        log.info("libraryEvent : {}", libraryEvent);

        switch (libraryEvent.getLibraryEventType()){
            case NEW:
                save(libraryEvent);
                break;
            case UPDATE:
                validate(libraryEvent);
                save(libraryEvent);
                break;

            default:log.info("Invalid library Event Type....");
        }
    }
    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        com.mariworld.springkafkademo.domain.LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), com.mariworld.springkafkademo.domain.LibraryEvent.class);
        Book bookEntity = Book.builder()
                .bookId(libraryEvent.getBook().getBookId())
                .bookAuthor(libraryEvent.getBook().getBookAuthor())
                .bookName(libraryEvent.getBook().getBookName())
                .build();

        LibraryEventType libraryEventType = LibraryEventType.valueOf(libraryEvent.getLibraryEventType().toString());

        LibraryEvent entity = LibraryEvent.builder()
                        .libraryEventId(libraryEvent.getLibraryEventId())
                        .book(bookEntity)
                        .libraryEventType(libraryEventType)
                        .build();
        log.info("libraryEvent : {}", libraryEvent);

        switch (libraryEvent.getLibraryEventType()){
            case NEW:
                save(entity);
                break;
            case UPDATE:
                validate(entity);
                break;

            default:log.info("Invalid library Event Type....");
        }
    }

    private void validate(LibraryEvent libraryEvent) {
        if(libraryEvent.getLibraryEventId()==null) throw new IllegalArgumentException("Library Event Id Could not be null..");

        Optional<LibraryEvent> libraryEvent1 = libraryEventRepository.findById(libraryEvent.getLibraryEventId());
        if(! libraryEvent1.isPresent()) throw new IllegalArgumentException("Library Event Could not be null..");

        log.info("Validation is Successful for the Library Event : {}", libraryEvent1.get());
    }

    private void save(LibraryEvent libraryEvent){
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventRepository.save(libraryEvent);
        log.info("Successfully Persisted the library Event : {}", libraryEvent);
    }

}
