package com.tuon.tbank.service.transaction;

import com.tuon.tbank.entity.transaction.Transfer;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.transaction.TransferRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransferService {

    private final TransferRepository transferRepository;

    public TransferService(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    public List<Transfer> getAllTransfers(){
        return transferRepository.findAll();
    }

    public Transfer getTransferById(Long id){
        Optional<Transfer> transfer = transferRepository.findById(id);
        return transfer.orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
}
