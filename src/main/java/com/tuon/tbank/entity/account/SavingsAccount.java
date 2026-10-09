package com.tuon.tbank.entity.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.enums.account.AccountStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a savings account, which is a type of bank account that typically earns interest on the deposited funds.
 */
@Entity
@Table(name = "savings_accounts")
@Getter
@Setter
public class SavingsAccount extends Account {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Client owner;

    private BigDecimal interestRate;

    public SavingsAccount() {
        super();
    }

    public SavingsAccount(Client owner, BigDecimal interestRate) {
        this.owner = owner;
        this.interestRate = interestRate;
    }

    public SavingsAccount(Long id, String accountNumber, BigDecimal balance, LocalDate openingDate, AccountStatus status, Client owner, BigDecimal interestRate) {
        super(id, accountNumber, balance, openingDate, status);
        this.owner = owner;
        this.interestRate = interestRate;
    }
}
