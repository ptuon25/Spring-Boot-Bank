package com.tuon.tbank.entity.transaction.reversalTransaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.person.Employee;
import com.tuon.tbank.entity.person.Person;
import com.tuon.tbank.entity.transaction.Transaction;
import com.tuon.tbank.enums.transaction.ReversalReason;
import com.tuon.tbank.enums.transaction.ReversalStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Represents a request to reverse a transaction and its review details.
 */
@Entity
@Getter
@Setter
@Table(name = "transaction_reversals")
public class TransactionReversal {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "original_transaction_id", nullable = false)
    private Transaction originalTransaction;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reversal_transaction_id", nullable = false)
    private Transaction reversalTransaction;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private Person requestedBy;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewed_by_id", nullable = false)
    private Employee reviewedBy;

    @Enumerated(EnumType.STRING)
    private ReversalStatus status;

    @Enumerated(EnumType.STRING)
    private ReversalReason reason;

    public TransactionReversal() {
    }

    public TransactionReversal(Long id, Transaction originalTransaction, Transaction reversalTransaction, Person requestedBy, Employee reviewedBy, ReversalStatus status, ReversalReason reason) {
        this.id = id;
        this.originalTransaction = originalTransaction;
        this.reversalTransaction = reversalTransaction;
        this.requestedBy = requestedBy;
        this.reviewedBy = reviewedBy;
        this.status = status;
        this.reason = reason;
    }

    public void approve(Employee reviewer){
        this.reviewedBy = reviewer;
        this.status = ReversalStatus.APPROVED;
    }

    public void reprove(Employee reviewer){
        this.reviewedBy = reviewer;
        this.status = ReversalStatus.REJECTED;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TransactionReversal that = (TransactionReversal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
