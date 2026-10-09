package com.tuon.tbank.controller.transaction;

import com.tuon.tbank.entity.transaction.reversalTransaction.TransactionReversal;
import com.tuon.tbank.service.transaction.TransactionReversalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transaction-reversals")
public class TransactionReversalController {

    private final TransactionReversalService transactionReversalService;

    public TransactionReversalController(TransactionReversalService transactionReversalService) {
        this.transactionReversalService = transactionReversalService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionReversal>> getAllTransactionReversals(){
        List<TransactionReversal> transactionReversals = transactionReversalService.getAllTransactionReversals();
        return ResponseEntity.ok().body(transactionReversals);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<TransactionReversal> getTransactionReversalById(@PathVariable Long id){
        TransactionReversal transactionReversal = transactionReversalService.getTransactionReversalById(id);
        return ResponseEntity.ok().body(transactionReversal);
    }
}
