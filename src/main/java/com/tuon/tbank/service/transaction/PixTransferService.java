package com.tuon.tbank.service.transaction;

import com.tuon.tbank.entity.transaction.PixTransfer;
import com.tuon.tbank.exception.ResourceNotFoundException;
import com.tuon.tbank.repository.transaction.PixTransferRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PixTransferService {

    private final PixTransferRepository pixTransferRepository;

    public PixTransferService(PixTransferRepository pixTransferRepository) {
        this.pixTransferRepository = pixTransferRepository;
    }

    public List<PixTransfer> getAllPixTransfers(){
        return pixTransferRepository.findAll();
    }

    public PixTransfer getPixTransferById(Long id){
        Optional<PixTransfer> pixTransfer = pixTransferRepository.findById(id);
        return pixTransfer.orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
}
