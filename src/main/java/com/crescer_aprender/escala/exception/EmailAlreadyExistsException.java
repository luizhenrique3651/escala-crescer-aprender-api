package com.crescer_aprender.escala.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EmailAlreadyExistsException extends ResponseStatusException {
    public EmailAlreadyExistsException(String email) {
        super(HttpStatus.BAD_REQUEST, "O e-mail " + email + " já está em uso.");
    }
}
