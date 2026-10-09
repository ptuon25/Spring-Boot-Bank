package com.tuon.tbank.controller.transaction;

import com.tuon.tbank.entity.transaction.external.ExternalBank;
import com.tuon.tbank.service.transaction.ExternalBankService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/external-banks")
public class ExternalBankController {

    private final ExternalBankService externalBankService;

    public ExternalBankController(ExternalBankService externalBankService) {
        this.externalBankService = externalBankService;
    }

    @GetMapping
    public ResponseEntity<List<ExternalBank>> getAllExternalBanks(){
        List<ExternalBank> externalBanks = externalBankService.getAllExternalBanks();
        return ResponseEntity.ok().body(externalBanks);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ExternalBank> getExternalBankById(@PathVariable Long id){
        ExternalBank externalBank = externalBankService.getExternalBankById(id);
        return ResponseEntity.ok().body(externalBank);
    }
}
