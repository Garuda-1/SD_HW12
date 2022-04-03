package ru.itmo.iandolzhanskii.sd.hw12.server.controller;

import javax.persistence.EntityNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {
    EntityNotFoundException.class
    })
    public ResponseEntity<Object> handeEntityNotFoundException() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {
    IllegalArgumentException.class
    })
    public ResponseEntity<Object> handeIllegalArgumentException() {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
