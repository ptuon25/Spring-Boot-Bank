package com.tuon.tbank.exception.serviceException;

import java.io.Serial;

/**
 * Signals that an investment operation is invalid.
 */
public class InvalidInvestmentException extends BusinessException{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidInvestmentException(String message){
        super(message);
    }
}
