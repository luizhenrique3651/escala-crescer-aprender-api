package com.crescer_aprender.escala.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserEmailNotFoundException extends ResponseStatusException {
    public UserEmailNotFoundException(String username) {
        super(HttpStatus.NOT_FOUND, "Usuário com email " + username + " não encontrado.");
    }
}
