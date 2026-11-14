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
 * Represents a checking account in the banking system.
 * This class extends the Account class and includes additional properties specific to checking accounts.
 */
@Entity
@Table(name = "checking_accounts")
@Getter
@Setter
public class CheckingAccount extends Account {

    /**
     * The owner of the checking account.
     * This is a many-to-one relationship with the Client entity.
     * The owner field is ignored during JSON serialization to prevent circular references.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Client owner;

    /**
     * Default constructor for the CheckingAccount class.
     * Initializes a new instance of the CheckingAccount class.
     */
    public CheckingAccount(){
        super();
    }

    /**
     * Constructs a new CheckingAccount with the specified parameters.
     *
     * @param id            the unique identifier of the account
     * @param accountNumber the account number
     * @param balance       the current balance of the account
     * @param openingDate   the date when the account was opened
     * @param status        the status of the account (e.g., ACTIVE, CLOSED)
     * @param owner         the client who owns the checking account
     */
    public CheckingAccount(Long id, String accountNumber, BigDecimal balance, LocalDate openingDate, AccountStatus status, Client owner) {
        super(id, accountNumber, balance, openingDate, status);
        this.owner = owner;
    }
}
