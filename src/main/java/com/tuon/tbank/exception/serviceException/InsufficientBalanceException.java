package com.tuon.tbank.exception.serviceException;

import java.io.Serial;

public class InsufficientBalanceException extends BusinessException{

    @Serial
    private static final long serialVersionUID = 1L;

    public InsufficientBalanceException(String message){
        super(message);
    }
}
