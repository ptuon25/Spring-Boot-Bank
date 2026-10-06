package com.tuon.tbank.repository.transaction;

import com.tuon.tbank.entity.transaction.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for withdrawal transactions.
 */
public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {
}