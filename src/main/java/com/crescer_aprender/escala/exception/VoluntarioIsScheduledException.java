package com.crescer_aprender.escala.exception;

public class VoluntarioIsScheduledException extends RuntimeException {
    public VoluntarioIsScheduledException() {
        super("Voluntário não pode ser deletado pois está escalado");
    }
}
