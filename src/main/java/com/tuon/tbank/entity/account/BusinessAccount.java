package com.tuon.tbank.entity.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.company.Company;
import com.tuon.tbank.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a business account, which is a type of bank account owned by a company.
 */
@Entity
@Table(name = "business_accounts")
@Getter
@Setter
public class BusinessAccount extends Account {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Company owner;

    public BusinessAccount(){
        super();
    }

    public BusinessAccount(Company owner) {
        this.owner = owner;
    }

    public BusinessAccount(Long id, String accountNumber, BigDecimal balance, LocalDate openingDate, AccountStatus status, Company owner) {
        super(id, accountNumber, balance, openingDate, status);
        this.owner = owner;
    }
}
