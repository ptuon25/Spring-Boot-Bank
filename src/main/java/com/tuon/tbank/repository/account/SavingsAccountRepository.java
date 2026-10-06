package com.tuon.tbank.repository.account;

import com.tuon.tbank.entity.account.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for savings accounts.
 */
public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long> {
}
