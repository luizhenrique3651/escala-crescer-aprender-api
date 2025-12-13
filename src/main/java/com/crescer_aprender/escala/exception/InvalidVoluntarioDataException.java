package com.crescer_aprender.escala.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidVoluntarioDataException extends ResponseStatusException {
    public InvalidVoluntarioDataException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
