package com.crescer_aprender.escala.exception;

import java.util.List;

public class VoluntarioNotExistException extends RuntimeException {
    public VoluntarioNotExistException(List<Long> ids) {
        super("Não foi possível cadastrar uma escala pois esses IDs de voluntário não estão cadastrados no banco: "+ ids.toString());
    }
}
