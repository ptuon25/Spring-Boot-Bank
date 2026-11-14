package com.tuon.tbank.entity.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.company.Company;
import com.tuon.tbank.enums.account.AccountStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a business account in the banking system.
 * This class extends the Account class and adds a reference to the owning company.
 */
@Entity
@Table(name = "business_accounts")
@Getter
@Setter
public class BusinessAccount extends Account {

    /**
     * The company that owns this business account.
     * This is a many-to-one relationship, as multiple business accounts can belong to a single company.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Company owner;

    /**
     * Default constructor for JPA.
     */
    public BusinessAccount() {
        super();
    }

    /**
     * Constructs a new BusinessAccount with the specified owner.
     *
     * @param owner the company that owns this business account
     */
    public BusinessAccount(Company owner) {
        this.owner = owner;
    }

    /**
     * Constructs a new BusinessAccount with the specified details.
     *
     * @param id            the unique identifier of the account
     * @param accountNumber the account number
     * @param balance       the current balance of the account
     * @param openingDate   the date when the account was opened
     * @param status        the current status of the account
     * @param owner         the company that owns this business account
     */
    public BusinessAccount(Long id, String accountNumber, BigDecimal balance, LocalDate openingDate, AccountStatus status, Company owner) {
        super(id, accountNumber, balance, openingDate, status);
        this.owner = owner;
    }
}
