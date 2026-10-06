package com.tuon.tbank.repository.transaction;

import com.tuon.tbank.entity.transaction.PixTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for Pix transfers.
 */
public interface PixTransferRepository extends JpaRepository<PixTransfer, Long> {
}