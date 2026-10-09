package com.tuon.tbank.entity.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.account.Account;
import com.tuon.tbank.enums.transaction.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a transaction that withdraws funds from a bank account.
 */
@Entity
@Getter
@Setter
@Table(name = "withdrawals")
public class Withdrawal extends Transaction {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    public Withdrawal() {
        super();
    }

    public Withdrawal(Account sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public Withdrawal(Long id, BigDecimal amount, LocalDateTime timestamp, TransactionStatus status, Account sourceAccount) {
        super(id, amount, timestamp, status);
        this.sourceAccount = sourceAccount;
    }
}
