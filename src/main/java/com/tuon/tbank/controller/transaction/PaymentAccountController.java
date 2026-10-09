package com.tuon.tbank.controller.transaction;

import com.tuon.tbank.entity.transaction.external.PaymentAccount;
import com.tuon.tbank.service.transaction.PaymentAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payment-accounts")
public class PaymentAccountController {

    private final PaymentAccountService paymentAccountService;

    public PaymentAccountController(PaymentAccountService paymentAccountService) {
        this.paymentAccountService = paymentAccountService;
    }
    
    @GetMapping
    public ResponseEntity<List<PaymentAccount>> getAllPaymentAccounts(){
        List<PaymentAccount> paymentAccounts = paymentAccountService.getAllPaymentAccounts();
        return ResponseEntity.ok().body(paymentAccounts);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<PaymentAccount> getPaymentAccountById(@PathVariable Long id){
        PaymentAccount paymentAccount = paymentAccountService.getPaymentAccountById(id);
        return ResponseEntity.ok().body(paymentAccount);
    }

}
