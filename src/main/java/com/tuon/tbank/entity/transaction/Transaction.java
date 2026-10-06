package com.tuon.tbank.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.audit.AuditRecord;
import com.tuon.tbank.entity.transaction.reversalTransaction.TransactionReversal;
import com.tuon.tbank.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a bank transaction.
 */
@Entity
@Getter
@Setter
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @OneToMany(mappedBy = "originalTransaction")
    private Set<TransactionReversal> originalTransactionReversals = new HashSet<>();

    @OneToMany(mappedBy = "reversalTransaction")
    private Set<TransactionReversal> reversalTransactionReversals = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "transaction")
    private Set<AuditRecord> auditRecords = new HashSet<>();

    public Transaction(){}

    public Transaction(Long id, BigDecimal amount, LocalDateTime timestamp, TransactionStatus status) {
        this.id = id;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
