package com.tuon.tbank.exception.serviceException;

import java.io.Serial;

/**
 * Signals that an operation cannot proceed because the available balance is insufficient.
 */
public class InsufficientBalanceException extends BusinessException{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with the specified detail message.
     *
     * @param message the detail message
     */
    public InsufficientBalanceException(String message){
        super(message);
    }
}
