package com.mariworld.springkafkademo.jpa;

import com.mariworld.springkafkademo.entity.LibraryEvent;
import org.springframework.data.repository.CrudRepository;

public interface LibraryEventRepository extends CrudRepository<LibraryEvent,Integer> {
}
