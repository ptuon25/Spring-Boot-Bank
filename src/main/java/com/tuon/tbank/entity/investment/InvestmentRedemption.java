package com.tuon.tbank.entity.investment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.account.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Represents the redemption of funds from an investment to an account.
 */
@Entity
@Getter
@Setter
@Table(name = "investment_redemption")
public class InvestmentRedemption extends InvestmentTransaction {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    private BigDecimal amount;

    public InvestmentRedemption() {
        super();
    }

    public InvestmentRedemption(Account sourceAccount, BigDecimal amount) {
        super();
        this.sourceAccount = sourceAccount;
        this.amount = amount;
    }
}
