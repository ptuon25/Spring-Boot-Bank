package com.tuon.tbank.entity.investment;

import com.tuon.tbank.entity.account.Account;
import com.tuon.tbank.enums.InvestmentType;
import jakarta.persistence.*;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    private InvestmentType type;

    @OneToMany(mappedBy = "portfolio")
    private Set<Investment> investments = new HashSet<>();

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
