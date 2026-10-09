package com.tuon.tbank.exception;

import java.io.Serial;

public class InvalidInvestmentException extends BusinessException{

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidInvestmentException(String message){
        super(message);
    }
}
