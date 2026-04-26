package com.crescer_aprender.escala.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CapacityViolationException extends RuntimeException {
    public CapacityViolationException(String message) {
        super(message);
    }
}
