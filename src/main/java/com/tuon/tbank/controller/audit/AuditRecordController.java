package com.tuon.tbank.controller.audit;

import com.tuon.tbank.entity.audit.AuditRecord;
import com.tuon.tbank.service.audit.AuditRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audits")
public class AuditRecordController {

    private final AuditRecordService auditRecordService;

    public AuditRecordController(AuditRecordService auditRecordService) {
        this.auditRecordService = auditRecordService;
    }

    @GetMapping
    public ResponseEntity<List<AuditRecord>> getAllAuditRecords(){
        List<AuditRecord> auditRecords = auditRecordService.getAllAuditRecords();
        return ResponseEntity.ok().body(auditRecords);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<AuditRecord> getAuditRecordById(@PathVariable Long id){
        AuditRecord auditRecord = auditRecordService.getAuditRecordById(id);
        return ResponseEntity.ok().body(auditRecord);
    }
}
