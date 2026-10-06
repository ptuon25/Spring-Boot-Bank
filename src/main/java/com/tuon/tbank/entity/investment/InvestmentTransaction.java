package com.tuon.tbank.entity.investment;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.account.Account;
import com.tuon.tbank.entity.transaction.Transaction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Represents a transaction associated with an investment.
 */
@Entity
@Getter
@Setter
@Table(name = "investment_transactions")
public class InvestmentTransaction extends Transaction {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investments_id", nullable = false)
    private Investment investment;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_accounts_id", nullable = false)
    private Account destinationAccount;

    private BigDecimal amount;

    public InvestmentTransaction(){}

    public InvestmentTransaction(Investment investment, Account destinationAccount, BigDecimal amount) {
        this.investment = investment;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
    }
}
