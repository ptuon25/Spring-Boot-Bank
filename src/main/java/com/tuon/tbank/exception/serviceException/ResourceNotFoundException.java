package com.tuon.tbank.exception.serviceException;

import java.io.Serial;

/**
 * Signals that a requested resource could not be found.
 */
public class ResourceNotFoundException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a resource-not-found exception with the specified message.
     *
     * @param message the detail message
     */
    public ResourceNotFoundException(String message){
        super(message);
    }
}
