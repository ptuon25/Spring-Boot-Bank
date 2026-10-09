package com.tuon.tbank.service.account;

import com.tuon.tbank.entity.account.SavingsAccount;
import com.tuon.tbank.exception.ResourceNotFoundException;
import com.tuon.tbank.repository.account.SavingsAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SavingsAccountService {

    private final SavingsAccountRepository savingsAccountRepository;

    public SavingsAccountService(SavingsAccountRepository savingsAccountRepository) {
        this.savingsAccountRepository = savingsAccountRepository;
    }

    public List<SavingsAccount> getAllSavingsAccounts(){
        return savingsAccountRepository.findAll();
    }

    public SavingsAccount getSavingsAccountById(Long id){
        Optional<SavingsAccount> savingsAccount = savingsAccountRepository.findById(id);
        return savingsAccount.orElseThrow(() -> new ResourceNotFoundException("Savings Account not found with id: " + id));
    }
}
