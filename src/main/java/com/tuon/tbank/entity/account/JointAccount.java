package com.tuon.tbank.entity.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.enums.account.AccountStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Represents a joint account in the banking system.
 * A joint account is an account that can be owned by multiple clients.
 */
@Entity
@Table(name = "joint_accounts")
@Getter
@Setter
public class JointAccount extends Account {

    /**
     * The list of clients who are owners of this joint account.
     * This is a many-to-many relationship, as multiple clients can own multiple joint accounts.
     */
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "joint_account_owner", joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "client_id"))
    private List<Client> owners;

    /**
     * Default constructor for the JointAccount class.
     * Initializes a new instance of the JointAccount class.
     */
    public JointAccount(){
        super();
    }

    /**
     * Constructs a new JointAccount with the specified details.
     *
     * @param id            the unique identifier of the account
     * @param accountNumber the account number
     * @param balance       the current balance of the account
     * @param openingDate   the date when the account was opened
     * @param status        the current status of the account
     * @param owners        the list of clients who own this joint account
     */
    public JointAccount(Long id, String accountNumber, BigDecimal balance, LocalDate openingDate, AccountStatus status, List<Client> owners) {
        super(id, accountNumber, balance, openingDate, status);
        this.owners = owners;
    }
}
