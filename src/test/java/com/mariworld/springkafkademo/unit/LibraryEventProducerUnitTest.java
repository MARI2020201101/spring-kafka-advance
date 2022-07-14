package com.mariworld.springkafkademo.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mariworld.springkafkademo.domain.Book;
import com.mariworld.springkafkademo.domain.LibraryEvent;
import com.mariworld.springkafkademo.producer.LibraryEventProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryEventProducerUnitTest {

    @InjectMocks
    LibraryEventProducer producer;
    @Mock
    KafkaTemplate<Integer,String> kafkaTemplate;
    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    // handleFailure 시나리오를 확인하는 단위 테스트
    @Test
    void sendLibraryEventV2_failure() throws JsonProcessingException, ExecutionException, InterruptedException {
        Book book = Book.builder()
                .bookAuthor("Dilip")
                .bookId(123)
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        SettableListenableFuture future = new SettableListenableFuture();
        future.setException(new RuntimeException("Exception calling kafka"));

        // 카프카 템플릿 일할필요없음 producer 의 비지니스 로직만을 검증하는 단위테스트이므로.. mock 으로 넣고 에러상황을 시뮬레이션한다.
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        assertThrows(Exception.class, ()-> producer.sendLibraryEventV2(libraryEvent).get());
    }

    @Test
    void sendLibraryEventV2_success() throws JsonProcessingException, ExecutionException, InterruptedException {
        Book book = Book.builder()
                .bookAuthor("Dilip")
                .bookId(123)
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String value = objectMapper.writeValueAsString(libraryEvent);

        SettableListenableFuture future = new SettableListenableFuture();
        ProducerRecord<Integer, String> producerRecord = new ProducerRecord<>(
                "demo-shyook-library-events"
                , libraryEvent.getLibraryEventId(), value);
        RecordMetadata recordMetadata = new RecordMetadata(
                new TopicPartition("demo-shyook-library-events"
                ,3)
                ,1,1,234,System.currentTimeMillis(),1,2);
        SendResult<Integer, String> sendResult = new SendResult<>(producerRecord, recordMetadata);
        future.set(sendResult);

        // happy 시나리오의 return 값을 테스트한다.
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
        SendResult<Integer, String> sendResult1 = producer.sendLibraryEventV2(libraryEvent).get();

        assertThat(sendResult1.getRecordMetadata().partition()==3);

    }
}
