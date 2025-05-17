package com.crescer_aprender.escala.exception;

public class EscalaAlreadyExistsException extends RuntimeException{
    public EscalaAlreadyExistsException(Long ano, Integer mes){
        super(String.format(ConstantExceptionUtil.ESCALA_ALREADY_EXISTS, mes, ano));
    }
}
