package com.tuon.tbank.entity.audit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.account.Account;
import com.tuon.tbank.entity.company.Company;
import com.tuon.tbank.entity.person.Person;
import com.tuon.tbank.entity.transaction.Transaction;
import com.tuon.tbank.enums.AuditAction;
import com.tuon.tbank.enums.AuditStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an audit record of an action performed in the banking system.
 */
@Entity
@Getter
@Setter
@Table(name = "audit_records")
public class AuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performed_by_id", nullable = false)
    private Person performedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    private AuditStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transactions_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accounts_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persons_id", nullable = false)
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "companies_id", nullable = false)
    private Company company;

    private String description;

    public AuditRecord(){}

    public AuditRecord(Long id, Person performedBy, LocalDateTime timestamp, AuditAction action, AuditStatus status, Transaction transaction, Account account, Person person, Company company, String description) {
        this.id = id;
        this.performedBy = performedBy;
        this.timestamp = timestamp;
        this.action = action;
        this.status = status;
        this.transaction = transaction;
        this.account = account;
        this.person = person;
        this.company = company;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AuditRecord that = (AuditRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
