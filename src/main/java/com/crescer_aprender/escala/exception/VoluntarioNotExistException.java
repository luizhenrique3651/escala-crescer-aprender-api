package com.crescer_aprender.escala.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public class VoluntarioNotExistException extends ResponseStatusException {
    public VoluntarioNotExistException(List<Long> ids) {
        super(HttpStatus.NOT_FOUND, "Não foi possível cadastrar uma escala pois esses IDs de voluntário não estão cadastrados no banco: "+ ids.toString());
    }
}
