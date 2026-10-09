package com.tuon.tbank.entity.transaction;

import com.tuon.tbank.entity.account.Account;
import com.tuon.tbank.enums.transaction.TransactionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a Pix transfer transaction.
 */
@Entity
@Getter
@Setter
@Table(name = "pix_transfers")
public class PixTransfer extends Transfer{
    String pixKey;

    public PixTransfer(){
        super();
    }

    public PixTransfer(Long id, BigDecimal amount, LocalDateTime timestamp, TransactionStatus status, Account sourceAccount, Account destinationAccount, String pixKey) {
        super(id, amount, timestamp, status, sourceAccount, destinationAccount);
        this.pixKey = pixKey;
    }
}
