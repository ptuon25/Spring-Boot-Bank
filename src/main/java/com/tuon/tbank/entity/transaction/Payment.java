package com.tuon.tbank.entity.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.account.Account;
import com.tuon.tbank.entity.transaction.external.PaymentAccount;
import com.tuon.tbank.enums.transaction.PaymentType;
import com.tuon.tbank.enums.transaction.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a payment transaction between two accounts.
 */
@Entity
@Getter
@Setter
@Table(name = "payments")
public class Payment extends Transaction{

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_paymentAccount_id", nullable = false)
    private PaymentAccount destination;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    public Payment(){
        super();
    }

    public Payment(Long id, BigDecimal amount, LocalDateTime timestamp, TransactionStatus status, Account sourceAccount, PaymentAccount destination, PaymentType paymentType) {
        super(id, amount, timestamp, status);
        this.sourceAccount = sourceAccount;
        this.destination = destination;
        this.paymentType = paymentType;
    }
}
