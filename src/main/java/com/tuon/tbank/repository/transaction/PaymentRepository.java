package com.tuon.tbank.repository.transaction;

import com.tuon.tbank.entity.transaction.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for payment transactions.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}