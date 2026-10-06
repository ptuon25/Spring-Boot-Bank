package com.tuon.tbank.entity.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.account.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Represents an international transfer transaction.
 */
@Entity
@Getter
@Setter
@Table(name = "international_transfers")
public class InternationalTransfer extends Transfer{

    BigDecimal exchangeRate;
    BigDecimal fee;

    public InternationalTransfer(){
        super();
    }

    public InternationalTransfer(BigDecimal exchangeRate, BigDecimal fee) {
        this.exchangeRate = exchangeRate;
        this.fee = fee;
    }
}
