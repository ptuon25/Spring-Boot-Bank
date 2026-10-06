package com.tuon.tbank.repository.audit;

import com.tuon.tbank.entity.audit.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Provides database access operations for audit records.
 */
public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long> {
}