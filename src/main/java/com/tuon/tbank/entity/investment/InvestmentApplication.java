package com.tuon.tbank.entity.investment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.account.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Represents the application of funds from an account to an investment.
 */
@Entity
@Getter
@Setter
@Table(name = "investment_applications")
public class InvestmentApplication extends InvestmentTransaction {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    private BigDecimal amount;

    public InvestmentApplication() {
        super();
    }

    public InvestmentApplication(Account sourceAccount, BigDecimal amount) {
        super();
        this.sourceAccount = sourceAccount;
        this.amount = amount;
    }
}
