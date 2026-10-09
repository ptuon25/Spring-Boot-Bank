package com.tuon.tbank.controller.account;

import com.tuon.tbank.entity.account.CheckingAccount;
import com.tuon.tbank.service.account.CheckingAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/checking-accounts")
public class CheckingAccountController {

    private final CheckingAccountService checkingAccountService;

    public CheckingAccountController(CheckingAccountService checkingAccountService) {
        this.checkingAccountService = checkingAccountService;
    }

    @GetMapping
    public ResponseEntity<List<CheckingAccount>> getAllCheckingAccounts(){
        List<CheckingAccount> checkingAccounts = checkingAccountService.getAllCheckingAccounts();
        return ResponseEntity.ok().body(checkingAccounts);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<CheckingAccount> getCheckingAccountById(@PathVariable Long id){
        CheckingAccount checkingAccount = checkingAccountService.getCheckingAccountById(id);
        return ResponseEntity.ok().body(checkingAccount);
    }
}
