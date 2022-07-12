package com.mariworld.springkafkademo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class LibraryEventControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleRequestBody(MethodArgumentNotValidException ex){
        List<FieldError> errors = ex.getBindingResult().getFieldErrors();

        String customErrorMessage = errors.stream()
                .map(error -> error.getField() + " - " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(" , "));

        log.error(" error !!! ----> {} ", customErrorMessage);
        return new ResponseEntity<>(customErrorMessage, HttpStatus.BAD_REQUEST);
    }
}
