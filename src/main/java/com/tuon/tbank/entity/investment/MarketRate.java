package com.tuon.tbank.entity.investment;

import com.tuon.tbank.enums.investment.MarketRateType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

/**
 * Represents a market rate recorded for a reference month.
 */
@Entity
@Getter
@Setter
@Table(name = "market-rates")
public class MarketRate {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MarketRateType type;

    private BigDecimal rate;
    private YearMonth referenceMonth;

    public MarketRate() {
    }

    public MarketRate(Long id, MarketRateType type, BigDecimal rate, YearMonth referenceMonth) {
        this.id = id;
        this.type = type;
        this.rate = rate;
        this.referenceMonth = referenceMonth;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MarketRate that = (MarketRate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
