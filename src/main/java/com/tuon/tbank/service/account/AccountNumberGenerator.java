package com.tuon.tbank.service.account;

import com.tuon.tbank.repository.account.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountNumberGenerator {

    private final AccountRepository accountRepository;

    public AccountNumberGenerator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public String generateUniqueAccountNumber(String prefix){
        Long number = accountRepository.getNextAccountNumber();
        return prefix + "-" + String.format("%08d", number);
    }
}
