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
import com.tuon.tbank.enums.account.AccountStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
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
    @Setter(AccessLevel.NONE)
    private Long id;

    private String accountNumber;

    @Setter(AccessLevel.NONE)
    private BigDecimal balance;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate openingDate;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "destinationAccount")
    private Set<Deposit> deposits = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "sourceAccount")
    private Set<Withdrawal> withdrawals = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "sourceAccount")
    private Set<Payment> payments = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "sourceAccount")
    private Set<Transfer> sourceTransfers = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "destinationAccount")
    private Set<Transfer> destinationTransfers = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "sourceAccount")
    private Set<ScheduledTransaction> destinationScheduledTransactions = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "destinationAccount")
    private Set<ScheduledTransaction> sourceScheduledTransactions = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "account")
    private Set<InvestmentPortfolio> investmentPortfolios = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "sourceAccount")
    private Set<InvestmentApplication> investmentApplications = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "sourceAccount")
    private Set<InvestmentRedemption> investmentRedemptions = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "destinationAccount")
    private Set<InvestmentTransaction> investmentTransactions = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @JsonIgnore
    @OneToMany(mappedBy = "account")
    private Set<AuditRecord> auditRecords = new HashSet<>();

    public Account(){

    }

    public Account(Long id, String accountNumber, BigDecimal balance, LocalDate openingDate, AccountStatus status) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.openingDate = openingDate;
        this.status = status;
    }

    public void credit (BigDecimal amount){
        this.balance = this.balance.add(amount);
    }

    public void debit (BigDecimal amount){
        this.balance = this.balance.subtract(amount);
    }

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

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? this.getClass().getSuperclass().hashCode()
                : getClass().hashCode();
    }
}
