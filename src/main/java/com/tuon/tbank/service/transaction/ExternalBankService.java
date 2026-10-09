package com.tuon.tbank.service.transaction;

import com.tuon.tbank.entity.transaction.external.ExternalBank;
import com.tuon.tbank.exception.ResourceNotFoundException;
import com.tuon.tbank.repository.transaction.ExternalBankRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExternalBankService {

    private final ExternalBankRepository externalBankRepository;

    public ExternalBankService(ExternalBankRepository externalBankRepository) {
        this.externalBankRepository = externalBankRepository;
    }

    public List<ExternalBank> getAllExternalBanks(){
        return externalBankRepository.findAll();
    }

    public ExternalBank getExternalBankById(Long id){
        Optional<ExternalBank> externalBank = externalBankRepository.findById(id);
        return externalBank.orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
}
