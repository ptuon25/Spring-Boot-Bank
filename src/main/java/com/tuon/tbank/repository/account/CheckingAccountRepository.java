package com.tuon.tbank.repository.account;

import com.tuon.tbank.entity.account.CheckingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for checking accounts.
 */
public interface CheckingAccountRepository extends JpaRepository<CheckingAccount, Long> {
}
