package com.tuon.tbank.exception.serviceException;

import java.io.Serial;

/**
 * Signals a business-rule violation reported by a service.
 */
public class BusinessException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a business exception with the specified message.
     *
     * @param message the detail message
     */
    public BusinessException(String message){
        super(message);
    }
}
