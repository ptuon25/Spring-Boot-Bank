package com.tuon.tbank.repository.transaction;

import com.tuon.tbank.entity.transaction.external.PaymentAccount;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for payment accounts.
 */
public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, Long> {
}