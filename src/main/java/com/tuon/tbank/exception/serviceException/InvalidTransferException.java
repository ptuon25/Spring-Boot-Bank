package com.tuon.tbank.exception.serviceException;

import java.io.Serial;

/**
 * Signals that a transfer operation is invalid.
 */
public class InvalidTransferException extends BusinessException{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidTransferException(String message){
        super(message);
    }
}
