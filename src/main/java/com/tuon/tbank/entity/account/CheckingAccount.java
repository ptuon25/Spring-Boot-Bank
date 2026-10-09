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
 * Represents a checking account, which is a type of bank account owned by an individual client.
 */
@Entity
@Table(name = "checking_accounts")
@Getter
@Setter
public class CheckingAccount extends Account {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Client owner;

    public CheckingAccount(){
        super();
    }

    public CheckingAccount(Long id, String accountNumber, BigDecimal balance, LocalDate openingDate, AccountStatus status, Client owner) {
        super(id, accountNumber, balance, openingDate, status);
        this.owner = owner;
    }
}
