package com.tuon.tbank.repository.investment;

import com.tuon.tbank.entity.investment.InvestmentPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for investment portfolios.
 */
public interface InvestmentPortfolioRepository extends JpaRepository<InvestmentPortfolio, Long> {
}