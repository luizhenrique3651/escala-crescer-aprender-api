package com.crescer_aprender.escala.exception;

public class ConstantExceptionUtil {
    public static final String ENTITY_NOT_FOUND = "%s com ID %d não encontrado.";
    public static final String EMAIL_ALREADY_EXISTS = "O e-mail %s já está em uso.";
    public static final String INVALID_VOLUNTARIO_NAME = "O nome do voluntário é obrigatório.";
    public static final String INVALID_VOLUNTARIO_EMAIL = "O e-mail do voluntário é inválido.";
    public static final String DATABASE_ERROR = "Erro ao acessar o banco de dados.";
    public static final String ESCALA_ALREADY_EXISTS = "A escala para %s/%s já existe";

    private ConstantExceptionUtil() {
    }
}

