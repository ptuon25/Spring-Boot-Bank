package com.tuon.tbank.entity.investment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tuon.tbank.enums.InvestmentRateType;
import com.tuon.tbank.enums.InvestmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an investment held in a portfolio.
 */
@Entity
@Getter
@Setter
@Table(name = "investments")
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private InvestmentPortfolio portfolio;

    private BigDecimal investedAmount;
    private BigDecimal currentValue;
    private BigDecimal interestRate;

    @Enumerated(EnumType.STRING)
    private InvestmentRateType rateType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate investmentDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate maturityDate;

    @Enumerated(EnumType.STRING)
    private InvestmentStatus status;

    @OneToMany(mappedBy = "investment")
    private Set<InvestmentMonthlyReturn> monthlyReturns = new HashSet<>();

    @OneToMany(mappedBy = "investment")
    private Set<InvestmentTransaction> investmentTransactions = new HashSet<>();

    private Investment() {
    }

    public Investment(Long id, InvestmentPortfolio portfolio, BigDecimal investedAmount, BigDecimal currentValue, InvestmentRateType rateType, BigDecimal interestRate, LocalDate investmentDate, LocalDate maturityDate, InvestmentStatus status) {
        this.id = id;
        this.portfolio = portfolio;
        this.investedAmount = investedAmount;
        this.currentValue = currentValue;
        this.rateType = rateType;
        this.interestRate = interestRate;
        this.investmentDate = investmentDate;
        this.maturityDate = maturityDate;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Investment that = (Investment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
