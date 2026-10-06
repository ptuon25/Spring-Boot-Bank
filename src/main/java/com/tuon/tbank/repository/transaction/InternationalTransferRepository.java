package com.tuon.tbank.repository.transaction;

import com.tuon.tbank.entity.transaction.InternationalTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for international transfers.
 */
public interface InternationalTransferRepository extends JpaRepository<InternationalTransfer, Long> {
}