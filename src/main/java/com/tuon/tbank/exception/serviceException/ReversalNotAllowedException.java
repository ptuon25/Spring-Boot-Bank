package com.tuon.tbank.exception.serviceException;

import java.io.Serial;

public class ReversalNotAllowedException extends BusinessException{

    @Serial
    private static final long serialVersionUID = 1L;

    public ReversalNotAllowedException(String message){
        super(message);
    }
}
