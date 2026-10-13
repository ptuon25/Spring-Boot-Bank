package com.tuon.tbank.exception.serviceException;

import java.io.Serial;

public class DatabaseException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = 1L;

    public DatabaseException(String message){
        super(message);
    }
}
