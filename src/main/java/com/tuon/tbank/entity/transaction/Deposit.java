package com.tuon.tbank.entity.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.account.Account;
import com.tuon.tbank.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a transaction that deposits funds into a bank account.
 */
@Entity
@Getter
@Setter
@Table(name = "deposits")
public class Deposit extends Transaction {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_account_id", nullable = false)
    Account destinationAccount;

    public Deposit(){
        super();
    }

    public Deposit(Account destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public Deposit(Long id, BigDecimal amount, LocalDateTime timestamp, TransactionStatus status, Account destinationAccount) {
        super(id, amount, timestamp, status);
        this.destinationAccount = destinationAccount;
    }
}
