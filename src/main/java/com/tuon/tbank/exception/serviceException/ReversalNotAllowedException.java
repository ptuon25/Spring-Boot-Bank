package com.tuon.tbank.exception.serviceException;

import java.io.Serial;

/**
 * Signals that a transaction reversal is not allowed.
 */
public class ReversalNotAllowedException extends BusinessException{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ReversalNotAllowedException(String message){
        super(message);
    }
}
