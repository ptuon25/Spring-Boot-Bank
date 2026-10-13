package com.tuon.tbank.service.transaction;

import com.tuon.tbank.entity.transaction.InternationalTransfer;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.transaction.InternationalTransferRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InternationalTransferService {

    private final InternationalTransferRepository internationalTransferRepository;

    public InternationalTransferService(InternationalTransferRepository internationalTransferRepository) {
        this.internationalTransferRepository = internationalTransferRepository;
    }

    public List<InternationalTransfer> getAllInternationalTransfers(){
        return internationalTransferRepository.findAll();
    }

    public InternationalTransfer getInternationalTransferById(Long id){
        Optional<InternationalTransfer> internationalTransfer = internationalTransferRepository.findById(id);
        return internationalTransfer.orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
}
