package com.tuon.tbank.service.account;

import com.tuon.tbank.repository.account.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountNumberGenerator {

    public static final String CHECKING_ACCOUNT_PREFIX = "CA";
    public static final String BUSINESS_ACCOUNT_PREFIX = "BA";
    public static final String SAVINGS_ACCOUNT_PREFIX = "SA";
    public static final String JOINT_ACCOUNT_PREFIX = "JA";

    private final AccountRepository accountRepository;

    public AccountNumberGenerator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public String generateUniqueAccountNumber(String prefix){
        Long number = accountRepository.getNextAccountNumber();
        return prefix + "-" + String.format("%08d", number);
    }
}
