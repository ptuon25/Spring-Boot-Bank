package com.tuon.tbank.exception.serviceException;

import java.io.Serial;

public class InvalidTransferException extends BusinessException{

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidTransferException(String message){
        super(message);
    }
}
