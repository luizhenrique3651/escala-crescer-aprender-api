package com.crescer_aprender.escala.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EscalaAlreadyExistsException extends ResponseStatusException {
    public EscalaAlreadyExistsException(Long ano, Integer mes){
        super(HttpStatus.BAD_REQUEST, String.format(ConstantExceptionUtil.ESCALA_ALREADY_EXISTS, mes, ano));
    }
}
