package com.tuon.tbank.controller.account;

import com.tuon.tbank.entity.account.BusinessAccount;
import com.tuon.tbank.service.account.BusinessAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/business-accounts")
public class BusinessAccountController {

    private final BusinessAccountService businessAccountService;

    public BusinessAccountController(BusinessAccountService businessAccountService) {
        this.businessAccountService = businessAccountService;
    }

    @GetMapping
    public ResponseEntity<List<BusinessAccount>> getAllBusinessAccount() {
        List<BusinessAccount> businessAccounts = businessAccountService.getAllBusinessAccounts();
        return ResponseEntity.ok().body(businessAccounts);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<BusinessAccount> getBusinessAccountById(@PathVariable Long id) {
        BusinessAccount businessAccount = businessAccountService.getBusinessAccountById(id);
        return ResponseEntity.ok().body(businessAccount);
    }

    @PostMapping
    public ResponseEntity<BusinessAccount> insertBusinessAccount(@RequestBody BusinessAccount businessAccount){
        BusinessAccount obj = businessAccountService.createBusinessAccount(businessAccount);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(obj.getId()).toUri();
        return ResponseEntity.created(uri).body(obj);
    }
}
