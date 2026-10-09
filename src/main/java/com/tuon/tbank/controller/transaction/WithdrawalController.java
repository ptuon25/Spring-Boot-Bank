package com.tuon.tbank.controller.transaction;

import com.tuon.tbank.entity.transaction.Withdrawal;
import com.tuon.tbank.service.transaction.WithdrawalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/withdrawals")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    public WithdrawalController(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }

    @GetMapping
    public ResponseEntity<List<Withdrawal>> getAllWithdrawals(){
        List<Withdrawal> withdrawals = withdrawalService.getAllWithdrawals();
        return ResponseEntity.ok().body(withdrawals);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Withdrawal> getWithdrawalById(@PathVariable Long id){
        Withdrawal withdrawal = withdrawalService.getWithdrawalById(id);
        return ResponseEntity.ok().body(withdrawal);
    }
}
