package com.tuon.tbank.service.account;

import com.tuon.tbank.entity.account.BusinessAccount;
import com.tuon.tbank.enums.account.AccountStatus;
import com.tuon.tbank.exception.serviceException.BusinessException;
import com.tuon.tbank.exception.serviceException.DatabaseException;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.account.BusinessAccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public BusinessAccount createBusinessAccount(BusinessAccount businessAccount){

        validateForCreate(businessAccount);
        try {
            return businessAccountRepository.saveAndFlush(businessAccount);
        } catch (DataIntegrityViolationException e){
            throw new DatabaseException("Could not create business account due to a database constraint.");
        }
    }

    private void validateForCreate(BusinessAccount businessAccount){
        if (businessAccount == null){
            throw new BusinessException("Account cannot be null");
        }
        if (businessAccount.getAccountNumber() == null || businessAccount.getAccountNumber().isBlank()){
            throw new BusinessException("Business Account number cannot be null or empty");
        }
        if (businessAccount.getBalance() == null || businessAccount.getBalance().compareTo(BigDecimal.ZERO) < 0){
            throw new BusinessException("Business Account balance cannot be null or negative");
        }
        if (businessAccount.getOpeningDate() == null){
            throw new BusinessException("Business Account opening date cannot be null");
        }
        if (businessAccount.getStatus() == null){
            throw new BusinessException("Business Account status cannot be null");
        }
        if (businessAccount.getStatus() != AccountStatus.ACTIVE) {
            throw  new BusinessException("Business Account must be created with ACTIVE status");
        }
        if (businessAccount.getOwner() == null){
            throw new BusinessException("Business Account must have an owner company");
        }
    }
}
