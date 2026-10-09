package com.tuon.tbank.entity.investment;

import com.tuon.tbank.entity.account.Account;
import com.tuon.tbank.enums.investment.InvestmentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an account's portfolio of investments.
 */
@Entity
@Getter
@Setter
@Table(name = "investment_portfolio")
public class InvestmentPortfolio {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    private InvestmentType type;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "portfolio")
    private Set<Investment> investments = new HashSet<>();

    public InvestmentPortfolio(){ }

    public InvestmentPortfolio(Long id, Account account, InvestmentType type, Set<Investment> investments) {
        this.id = id;
        this.account = account;
        this.type = type;
        this.investments = investments;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        InvestmentPortfolio that = (InvestmentPortfolio) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
