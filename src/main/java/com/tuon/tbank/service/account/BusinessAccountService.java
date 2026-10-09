package com.tuon.tbank.service.account;

import com.tuon.tbank.entity.account.BusinessAccount;
import com.tuon.tbank.exception.ResourceNotFoundException;
import com.tuon.tbank.repository.account.BusinessAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BusinessAccountService {

    private final BusinessAccountRepository businessAccountRepository;

    public BusinessAccountService(BusinessAccountRepository businessAccountRepository) {
        this.businessAccountRepository = businessAccountRepository;
    }

    public List<BusinessAccount> getAllBusinessAccounts(){
        return businessAccountRepository.findAll();
    }

    public BusinessAccount getBusinessAccountById(Long id){
        Optional<BusinessAccount> businessAccount = businessAccountRepository.findById(id);
        return businessAccount.orElseThrow(() -> new ResourceNotFoundException("Business Account not found with id: " + id));
    }
}
