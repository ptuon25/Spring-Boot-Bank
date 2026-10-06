package com.tuon.tbank.repository.transaction;

import com.tuon.tbank.entity.transaction.external.ExternalBank;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for external banks.
 */
public interface ExternalBankRepository extends JpaRepository<ExternalBank, Long> {
}