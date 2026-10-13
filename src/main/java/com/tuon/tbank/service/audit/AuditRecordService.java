package com.tuon.tbank.service.audit;

import com.tuon.tbank.entity.audit.AuditRecord;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.audit.AuditRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuditRecordService {

    private final AuditRecordRepository auditRecordRepository;

    public AuditRecordService(AuditRecordRepository auditRecordRepository) {
        this.auditRecordRepository = auditRecordRepository;
    }

    public List<AuditRecord> getAllAuditRecords(){
        return auditRecordRepository.findAll();
    }

    public AuditRecord getAuditRecordById(Long id){
        Optional<AuditRecord> auditRecord = auditRecordRepository.findById(id);
        return auditRecord.orElseThrow(() -> new ResourceNotFoundException("Audit record not found with id: " + id));
    }
}
