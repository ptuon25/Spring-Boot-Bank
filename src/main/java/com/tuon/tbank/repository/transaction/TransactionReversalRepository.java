package com.tuon.tbank.repository.transaction;

import com.tuon.tbank.entity.transaction.reversalTransaction.TransactionReversal;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for transaction reversal requests.
 */
public interface TransactionReversalRepository extends JpaRepository<TransactionReversal, Long> {
}