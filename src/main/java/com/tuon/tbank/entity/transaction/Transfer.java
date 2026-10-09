package com.tuon.tbank.entity.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.account.Account;
import com.tuon.tbank.entity.transaction.external.ExternalAccount;
import com.tuon.tbank.enums.transaction.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a transfer transaction between two accounts.
 */
@Entity
@Getter
@Setter
@Table(name = "transfers")
public class Transfer extends Transaction {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_external_account_id")
    private ExternalAccount sourceExternalAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_external_account_id")
    private ExternalAccount destinationExternalAccount;

    public Transfer(){
        super();
    }

    public Transfer(Account sourceAccount, Account destinationAccount) {
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
    }

    public Transfer(Long id, BigDecimal amount, LocalDateTime timestamp, TransactionStatus status, Account sourceAccount, Account destinationAccount) {
        super(id, amount, timestamp, status);
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
    }
}
