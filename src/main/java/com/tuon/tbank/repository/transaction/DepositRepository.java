package com.tuon.tbank.repository.transaction;

import com.tuon.tbank.entity.transaction.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for deposit transactions.
 */
public interface DepositRepository extends JpaRepository<Deposit, Long> {
}