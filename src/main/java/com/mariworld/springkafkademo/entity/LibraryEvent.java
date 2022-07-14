package com.mariworld.springkafkademo.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class LibraryEvent {
    @Id
    @GeneratedValue
    private Integer libraryEventId;

    @OneToOne(mappedBy = "libraryEvent", cascade = {CascadeType.ALL})
    @ToString.Exclude
    private Book book;

    @Enumerated(EnumType.STRING)
    private LibraryEventType libraryEventType;
}
