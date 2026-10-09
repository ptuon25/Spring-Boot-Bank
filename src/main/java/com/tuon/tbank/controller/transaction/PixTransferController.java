package com.tuon.tbank.controller.transaction;

import com.tuon.tbank.entity.transaction.PixTransfer;
import com.tuon.tbank.service.transaction.PixTransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pix-transfers")
public class PixTransferController {

    private final PixTransferService pixTransferService;

    public PixTransferController(PixTransferService pixTransferService) {
        this.pixTransferService = pixTransferService;
    }

    @GetMapping
    public ResponseEntity<List<PixTransfer>> getAllPixTransfers(){
        List<PixTransfer> pixTransfers = pixTransferService.getAllPixTransfers();
        return ResponseEntity.ok().body(pixTransfers);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<PixTransfer> getPixTransferById(@PathVariable Long id){
        PixTransfer pixTransfer = pixTransferService.getPixTransferById(id);
        return ResponseEntity.ok().body(pixTransfer);
    }
}
