package com.crescer_aprender.escala.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("O e-mail " + email + " já está em uso.");
    }
}
