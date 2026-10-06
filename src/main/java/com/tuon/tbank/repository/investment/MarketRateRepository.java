package com.tuon.tbank.repository.investment;

import com.tuon.tbank.entity.investment.MarketRate;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for market rates.
 */
public interface MarketRateRepository extends JpaRepository<MarketRate, Long> {
}