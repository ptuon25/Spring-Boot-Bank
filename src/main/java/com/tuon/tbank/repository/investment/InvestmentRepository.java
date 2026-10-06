package com.tuon.tbank.repository.investment;

import com.tuon.tbank.entity.investment.Investment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for investments.
 */
public interface InvestmentRepository extends JpaRepository<Investment, Long> {
}