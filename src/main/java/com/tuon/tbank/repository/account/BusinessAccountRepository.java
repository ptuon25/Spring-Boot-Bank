package com.tuon.tbank.repository.account;

import com.tuon.tbank.entity.account.BusinessAccount;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for business accounts.
 */
public interface BusinessAccountRepository extends JpaRepository<BusinessAccount, Long> {
}
