package com.tuon.tbank.repository.investment;

import com.tuon.tbank.entity.investment.InvestmentMonthlyReturn;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for investment monthly returns.
 */
public interface InvestmentMonthlyReturnRepository extends JpaRepository<InvestmentMonthlyReturn, Long> {
}