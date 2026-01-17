package com.crescer_aprender.escala.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class VoluntarioIsScheduledException extends ResponseStatusException {
    public VoluntarioIsScheduledException() {
        super(HttpStatus.BAD_REQUEST, "Voluntário não pode ser deletado pois está escalado");
    }
}
