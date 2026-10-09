package com.tuon.tbank.controller.account;

import com.tuon.tbank.entity.account.SavingsAccount;
import com.tuon.tbank.service.account.SavingsAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/savings-accounts")
public class SavingsAccountController {

    private final SavingsAccountService savingsAccountService;

    public SavingsAccountController(SavingsAccountService savingsAccountService) {
        this.savingsAccountService = savingsAccountService;
    }

    @GetMapping
    public ResponseEntity<List<SavingsAccount>> getAllSavingsAccounts(){
        List<SavingsAccount> savingsAccounts = savingsAccountService.getAllSavingsAccounts();
        return ResponseEntity.ok().body(savingsAccounts);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<SavingsAccount> getSavingsAccountById(@PathVariable Long id){
        SavingsAccount savingsAccount = savingsAccountService.getSavingsAccountById(id);
        return ResponseEntity.ok().body(savingsAccount);
    }
}
