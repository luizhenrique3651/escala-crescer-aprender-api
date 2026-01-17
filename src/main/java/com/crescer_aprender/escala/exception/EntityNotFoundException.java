package com.crescer_aprender.escala.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EntityNotFoundException extends ResponseStatusException {
    public EntityNotFoundException(String entity, Long id) {
        super(HttpStatus.NOT_FOUND, String.format(ConstantExceptionUtil.ENTITY_NOT_FOUND, entity, id));
    }

    public EntityNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
