package com.tuon.tbank.service.account;

import com.tuon.tbank.entity.account.CheckingAccount;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.account.CheckingAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CheckingAccountService {

    private final CheckingAccountRepository checkingAccountRepository;

    public CheckingAccountService(CheckingAccountRepository checkingAccountRepository) {
        this.checkingAccountRepository = checkingAccountRepository;
    }

    public List<CheckingAccount> getAllCheckingAccounts(){
        return checkingAccountRepository.findAll();
    }

    public CheckingAccount getCheckingAccountById(Long id){
        Optional<CheckingAccount> checkingAccount = checkingAccountRepository.findById(id);
        return checkingAccount.orElseThrow(() -> new ResourceNotFoundException("Checking Account not found with id: " + id));
    }
}
