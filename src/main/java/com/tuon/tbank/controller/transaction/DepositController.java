package com.tuon.tbank.controller.transaction;

import com.tuon.tbank.entity.transaction.Deposit;
import com.tuon.tbank.service.transaction.DepositService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/deposits")
public class DepositController {

    private final DepositService depositService;

    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    @GetMapping
    public ResponseEntity<List<Deposit>> getAllDeposits(){
        List<Deposit> deposits = depositService.getAllDeposits();
        return ResponseEntity.ok().body(deposits);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Deposit> getDepositById(@PathVariable Long id){
        Deposit deposit = depositService.getDepositById(id);
        return ResponseEntity.ok().body(deposit);
    }
}
