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
 * Represents a savings account in the banking system.
 * Inherits from the Account class and adds specific attributes for savings accounts.
 */
@Entity
@Table(name = "savings_accounts")
@Getter
@Setter
public class SavingsAccount extends Account {

    /**
     * The client who owns this savings account.
     * This is a many-to-one relationship, as a client can have multiple savings accounts.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Client owner;

    /**
     * The interest rate for the savings account.
     * <p>This is the annual percentage rate (APR) at which the account earns interest.</p>
     */
    private BigDecimal interestRate;

    /**
     * Default constructor for JPA.
     * Initializes a new instance of the SavingsAccount class.
     */
    public SavingsAccount() {
        super();
    }

    /**
     * Constructs a new SavingsAccount with the specified details.
     *
     * @param id            the unique identifier of the account
     * @param accountNumber the account number
     * @param balance       the current balance of the account
     * @param openingDate   the date when the account was opened
     * @param status        the current status of the account (e.g., ACTIVE, CLOSED)
     * @param owner         the client who owns this savings account
     * @param interestRate  the interest rate for this savings account
     */
    public SavingsAccount(Long id, String accountNumber, BigDecimal balance, LocalDate openingDate, AccountStatus status, Client owner, BigDecimal interestRate) {
        super(id, accountNumber, balance, openingDate, status);
        this.owner = owner;
        this.interestRate = interestRate;
    }
}
