package com.tuon.tbank.repository.transaction;

import com.tuon.tbank.entity.transaction.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for transfer transactions.
 */
public interface TransferRepository extends JpaRepository<Transfer, Long> {
}