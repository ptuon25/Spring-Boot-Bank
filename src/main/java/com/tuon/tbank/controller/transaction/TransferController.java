package com.tuon.tbank.controller.transaction;

import com.tuon.tbank.entity.transaction.Transfer;
import com.tuon.tbank.service.transaction.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping
    public ResponseEntity<List<Transfer>> getAllTransfers(){
        List<Transfer> transfers = transferService.getAllTransfers();
        return ResponseEntity.ok().body(transfers);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Transfer> getTransferById(@PathVariable Long id){
        Transfer transfer = transferService.getTransferById(id);
        return ResponseEntity.ok().body(transfer);
    }
}
