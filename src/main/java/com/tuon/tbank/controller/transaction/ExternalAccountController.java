package com.tuon.tbank.controller.transaction;

import com.tuon.tbank.entity.transaction.external.ExternalAccount;
import com.tuon.tbank.service.transaction.ExternalAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/external-accounts")
public class ExternalAccountController {

    private final ExternalAccountService externalAccountService;

    public ExternalAccountController(ExternalAccountService externalAccountService) {
        this.externalAccountService = externalAccountService;
    }

    @GetMapping
    public ResponseEntity<List<ExternalAccount>> getAllExternalAccounts(){
        List<ExternalAccount> externalAccounts = externalAccountService.getAllExternalAccounts();
        return ResponseEntity.ok().body(externalAccounts);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ExternalAccount> getExternalAccountById(@PathVariable Long id){
        ExternalAccount externalAccount = externalAccountService.getExternalAccountById(id);
        return ResponseEntity.ok().body(externalAccount);
    }
}
