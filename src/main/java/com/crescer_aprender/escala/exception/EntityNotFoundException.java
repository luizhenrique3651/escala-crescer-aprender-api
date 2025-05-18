package com.crescer_aprender.escala.exception;

public class EntityNotFoundException extends RuntimeException{
    public EntityNotFoundException(String entity, Long id) {
        super(String.format(ConstantExceptionUtil.ENTITY_NOT_FOUND, entity, id));
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
