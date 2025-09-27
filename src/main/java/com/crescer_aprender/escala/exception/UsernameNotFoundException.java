package com.crescer_aprender.escala.exception;

public class UsernameNotFoundException extends RuntimeException{
    public UsernameNotFoundException(String username) {
        super("Usuário com email " + username + " não encontrado.");
    }
}
