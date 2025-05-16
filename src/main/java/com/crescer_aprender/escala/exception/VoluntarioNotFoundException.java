package com.crescer_aprender.escala.exception;

public class VoluntarioNotFoundException extends RuntimeException {
    public VoluntarioNotFoundException(Long id) {
        super(String.format(ConstantExceptionUtil.VOLUNTARIO_NOT_FOUND, id));
    }
}


