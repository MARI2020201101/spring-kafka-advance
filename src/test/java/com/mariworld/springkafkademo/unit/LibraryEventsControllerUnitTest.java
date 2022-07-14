package com.mariworld.springkafkademo.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mariworld.springkafkademo.controller.LibraryEventController;
import com.mariworld.springkafkademo.domain.Book;
import com.mariworld.springkafkademo.domain.LibraryEvent;
import com.mariworld.springkafkademo.producer.LibraryEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(LibraryEventController.class)// 컨트롤러 클래스 mock
@AutoConfigureMockMvc // 클라이언트 mock
public class LibraryEventsControllerUnitTest {

    @Autowired
    MockMvc mockMvc; // 엔드포인트를 찌르는 mockMvc (클라이언트 흉내)
    ObjectMapper objectMapper = new ObjectMapper(); // json string 으로 만들어줄것임
    @MockBean
    LibraryEventProducer libraryEventProducer ; // Autowired 를 mock


    @Test
    void postLibraryEvent() throws Exception {
        Book book = Book.builder()
                .bookAuthor("Dilip")
                .bookId(1234)
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);

        //컨트롤러에 대한 단위 테스트. 그 안의 Autowired 된 빈의 행동은 신경쓰지 않는다.
//        doNothing().when(libraryEventProducer)
//                .sendLibraryEventV2(isA(LibraryEvent.class));
        when(libraryEventProducer.sendLibraryEventV2(isA(LibraryEvent.class))).thenReturn(null);

        mockMvc.perform(post("/v1/library-event")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }@Test
    void postLibraryEvent_4xx() throws Exception {
        Book book = Book.builder()
                .bookAuthor(null)
                .bookId(null)
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);

//        doNothing().when(libraryEventProducer)
//                .sendLibraryEventV2(isA(LibraryEvent.class));
        when(libraryEventProducer.sendLibraryEventV2(isA(LibraryEvent.class))).thenReturn(null);

        String expectedErrorMessage = "book.bookAuthor - must not be blank , book.bookId - must not be null";
        mockMvc.perform(post("/v1/library-event")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(expectedErrorMessage));
    }
}
