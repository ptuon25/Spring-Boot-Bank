package com.tuon.tbank.service.transaction;

import com.tuon.tbank.entity.transaction.external.ExternalAccount;
import com.tuon.tbank.exception.ResourceNotFoundException;
import com.tuon.tbank.repository.transaction.ExternalAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExternalAccountService {

    private final ExternalAccountRepository externalAccountRepository;

    public ExternalAccountService(ExternalAccountRepository externalAccountRepository) {
        this.externalAccountRepository = externalAccountRepository;
    }

    public List<ExternalAccount> getAllExternalAccounts(){
        return externalAccountRepository.findAll();
    }

    public ExternalAccount getExternalAccountById(Long id){
        Optional<ExternalAccount> externalAccount = externalAccountRepository.findById(id);
        return externalAccount.orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
}
