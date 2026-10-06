package com.tuon.tbank.entity.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.audit.AuditRecord;
import com.tuon.tbank.entity.investment.InvestmentApplication;
import com.tuon.tbank.entity.investment.InvestmentPortfolio;
import com.tuon.tbank.entity.investment.InvestmentRedemption;
import com.tuon.tbank.entity.investment.InvestmentTransaction;
import com.tuon.tbank.entity.transaction.*;
import com.tuon.tbank.entity.transaction.endpoint.TransferEndpoint;
import com.tuon.tbank.entity.transaction.scheduledTransaction.ScheduledTransaction;
import com.tuon.tbank.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a bank account.
 */
@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "accounts")
public abstract class Account implements TransferEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;
    private BigDecimal balance;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
    private LocalDate openingDate;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @OneToMany(mappedBy = "destinationAccount")
    private Set<Deposit> deposits = new HashSet<>();

    @OneToMany(mappedBy = "sourceAccount")
    private Set<Withdrawal> withdrawals = new HashSet<>();

    @OneToMany(mappedBy = "sourceAccount")
    private Set<Payment> payments = new HashSet<>();

    @OneToMany(mappedBy = "sourceAccount")
    private Set<Transfer> sourceTransfers = new HashSet<>();

    @OneToMany(mappedBy = "destinationAccount")
    private Set<Transfer> destinationTransfers = new HashSet<>();

    @OneToMany(mappedBy = "sourceAccount")
    private Set<ScheduledTransaction> destinationScheduledTransactions = new HashSet<>();

    @OneToMany(mappedBy = "destinationAccount")
    private Set<ScheduledTransaction> sourceScheduledTransactions = new HashSet<>();

    @OneToMany(mappedBy = "account")
    private Set<InvestmentPortfolio> investmentPortfolios = new HashSet<>();

    @OneToMany(mappedBy = "sourceAccount")
    private Set<InvestmentApplication> investmentApplications = new HashSet<>();

    @OneToMany(mappedBy = "sourceAccount")
    private Set<InvestmentRedemption> investmentRedemptions = new HashSet<>();

    @OneToMany(mappedBy = "destinationAccount")
    private Set<InvestmentTransaction> investmentTransactions = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "account")
    private Set<AuditRecord> auditRecords = new HashSet<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? o.getClass().getSuperclass()
                : o.getClass();

        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? this.getClass().getSuperclass()
                : this.getClass();

        if (thisEffectiveClass != oEffectiveClass) return false;

        Account account = (Account) o;

        return getId() != null && Objects.equals(getId(), account.getId());
    }

    public Account(){

    }

    public Account(Long id, String accountNumber, BigDecimal balance, LocalDate openingDate, AccountStatus status) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.openingDate = openingDate;
        this.status = status;
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? this.getClass().getSuperclass().hashCode()
                : getClass().hashCode();
    }
}
