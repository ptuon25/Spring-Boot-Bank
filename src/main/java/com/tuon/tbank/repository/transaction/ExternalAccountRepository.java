package com.tuon.tbank.repository.transaction;

import com.tuon.tbank.entity.transaction.external.ExternalAccount;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for external bank accounts.
 */
public interface ExternalAccountRepository extends JpaRepository<ExternalAccount, Long> {
}