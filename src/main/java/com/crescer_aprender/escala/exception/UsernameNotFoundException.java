package com.crescer_aprender.escala.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UsernameNotFoundException extends ResponseStatusException {
    public UsernameNotFoundException(String username) {
        super(HttpStatus.NOT_FOUND, "Usuário com email " + username + " não encontrado.");
    }
}
