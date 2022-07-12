package com.mariworld.springkafkademo.integration;

import com.mariworld.springkafkademo.domain.Book;
import com.mariworld.springkafkademo.domain.LibraryEvent;
import kafka.Kafka;
import kafka.KafkaTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@EmbeddedKafka(topics = {"demo-shyook-library-events"}, partitions = 10)
@TestPropertySource(properties =
        {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}"
, "spring.kafka.admin.properties.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventsControllerIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;
    private Consumer<Integer, String> consumer;

    @BeforeEach
    void setUp(){
        Map<String , Object> configs = new HashMap<>(KafkaTestUtils.consumerProps(
                "test-consumer-group", "true", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(
                configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown(){
        consumer.close();
    }

    @Test
    public void postLibraryEvent() throws InterruptedException {
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(Book.builder()
                            .bookAuthor("Dilip")
                            .bookId(1234)
                            .bookName("Kafka Using Spring Boot")
                            .build())
                .build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LibraryEvent> entity = new HttpEntity<>(libraryEvent, httpHeaders);

        ResponseEntity<LibraryEvent> response = restTemplate.exchange("/v1/library-event", HttpMethod.POST, entity, LibraryEvent.class);
        log.info("headers : {}", response.getHeaders());
        log.info("body : {}", response.getBody());
        log.info("status code : {}", response.getStatusCode());

        assertThat(response.getStatusCode() , is(HttpStatus.CREATED));

        ConsumerRecord<Integer, String> record = KafkaTestUtils.getSingleRecord(consumer, "demo-shyook-library-events");

        Thread.sleep(1000);
        log.warn("record value : {} ", record.value());
        String value = "\"LibraryEvent(libraryEventId=null, book=Book(bookId=1234, bookName=Kafka Using Spring Boot, bookAuthor=Dilip), libraryEventType=NEW)\"";
        assertThat(record.value() , is(value));

    }
}