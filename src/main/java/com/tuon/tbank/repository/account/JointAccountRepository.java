package com.tuon.tbank.repository.account;

import com.tuon.tbank.entity.account.JointAccount;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for joint accounts.
 */
public interface JointAccountRepository extends JpaRepository<JointAccount, Long> {
}
