package com.tuon.tbank.entity.transaction;

import com.tuon.tbank.entity.account.Account;
import com.tuon.tbank.entity.transaction.external.ExternalAccount;
import com.tuon.tbank.enums.transaction.TransactionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents an international transfer transaction.
 */
@Entity
@Getter
@Setter
@Table(name = "international_transfers")
public class InternationalTransfer extends Transfer {

    BigDecimal exchangeRate;
    BigDecimal fee;

    public InternationalTransfer() {
        super();
    }

    public InternationalTransfer(Long id, BigDecimal amount, LocalDateTime timestamp, TransactionStatus status, Account sourceAccount, ExternalAccount destinationExternalAccount, BigDecimal exchangeRate, BigDecimal fee) {
        super(id, amount, timestamp, status, sourceAccount, null);
        this.setDestinationExternalAccount(destinationExternalAccount);
        this.exchangeRate = exchangeRate;
        this.fee = fee;
    }

    public InternationalTransfer(Long id, BigDecimal amount, LocalDateTime timestamp, TransactionStatus status, ExternalAccount sourceExternalAccount, Account destinationAccount, BigDecimal exchangeRate, BigDecimal fee) {

        super(id, amount, timestamp, status, null, destinationAccount);
        this.setSourceExternalAccount(sourceExternalAccount);
        this.exchangeRate = exchangeRate;
        this.fee = fee;
    }

}
