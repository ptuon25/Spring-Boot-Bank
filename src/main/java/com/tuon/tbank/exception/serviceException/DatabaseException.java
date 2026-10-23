package com.tuon.tbank.exception.serviceException;

import java.io.Serial;

/**
 * Signals a database-related failure reported by a service.
 */
public class DatabaseException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a database exception with the specified message.
     *
     * @param message the detail message
     */
    public DatabaseException(String message){
        super(message);
    }
}
