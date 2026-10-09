package com.tuon.tbank.controller.transaction;

import com.tuon.tbank.entity.transaction.InternationalTransfer;
import com.tuon.tbank.service.transaction.InternationalTransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/international-transfers")
public class InternationalTransferController {

    private final InternationalTransferService internationalTransferService;

    public InternationalTransferController(InternationalTransferService internationalTransferService) {
        this.internationalTransferService = internationalTransferService;
    }

    @GetMapping
    public ResponseEntity<List<InternationalTransfer>> getAllInternationalTransfers(){
        List<InternationalTransfer> internationalTransfers = internationalTransferService.getAllInternationalTransfers();
        return ResponseEntity.ok().body(internationalTransfers);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<InternationalTransfer> getInternationalTransferById(@PathVariable Long id){
        InternationalTransfer internationalTransfer = internationalTransferService.getInternationalTransferById(id);
        return ResponseEntity.ok().body(internationalTransfer);
    }
}
