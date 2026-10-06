package com.tuon.tbank.entity.investment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

/**
 * Represents the return earned by an investment during a reference month.
 */
@Getter
@Setter
@Entity
@Table(name = "investment_monthly_return")
public class InvestmentMonthlyReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investment_id", nullable = false)
    private Investment investment;

    private YearMonth referenceMonth;
    private BigDecimal amount;

    public InvestmentMonthlyReturn(){}

    public InvestmentMonthlyReturn(Long id, Investment investment, YearMonth referenceMonth, BigDecimal amount) {
        this.id = id;
        this.investment = investment;
        this.referenceMonth = referenceMonth;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        InvestmentMonthlyReturn that = (InvestmentMonthlyReturn) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
