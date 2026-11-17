package com.tuon.tbank.service.account;

import com.tuon.tbank.entity.account.Account;
import com.tuon.tbank.enums.account.AccountStatus;
import com.tuon.tbank.exception.serviceException.BusinessException;
import com.tuon.tbank.exception.serviceException.InsufficientBalanceException;
import com.tuon.tbank.exception.serviceException.InvalidTransferException;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.account.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing accounts, providing business logic and operations related to account management.
 *
 * <p>This service provides methods for creating, updating, and retrieving account information.</p>
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * Constructs an instance of {@link AccountService} with the specified {@link AccountRepository}.
     *
     * @param accountRepository the repository for accessing account data
     */
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Retrieves all accounts from the database.
     *
     * @return a list of all accounts
     */
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    /**
     * Retrieves all accounts that are not marked as deleted.
     *
     * @return a list of non-deleted accounts
     */
    public List<Account> getAllValidAccounts() {
        return accountRepository.findByDeletedFalse();
    }

    /**
     * Retrieves all accounts that are marked as deleted.
     *
     * @return a list of deleted accounts
     */
    public List<Account> getAllDeletedAccounts() {
        return accountRepository.findByDeletedTrue();
    }

    /**
     * Retrieves an account by its ID, ensuring it is not marked as deleted.
     *
     * @param id the ID of the account to retrieve
     * @return the found account
     * @throws BusinessException if the ID is null or invalid
     * @throws ResourceNotFoundException if no account is found with the given ID
     */
    public Account getAccountById(Long id) {
        if(id == null || id <= 0) {
            throw new BusinessException("Account ID cannot be null.");
        }
        Optional<Account> account = accountRepository.findByIdAndDeletedFalse(id);
        return account.orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    /**
     * Retrieves an account by its account number, ensuring it is not marked as deleted.
     *
     * @param accountNumber the account number of the account to retrieve
     * @return the found account
     * @throws BusinessException if the account number is null or empty
     * @throws ResourceNotFoundException if no account is found with the given account number
     */
    public Account getAccountByAccountNumber(String accountNumber) {
        if(accountNumber == null || accountNumber.isEmpty()) {
            throw new BusinessException("Account number cannot be null or empty.");
        }
        Optional<Account> account = accountRepository.findByAccountNumberAndDeletedFalse(accountNumber);
        return account.orElseThrow(() -> new ResourceNotFoundException("Account not found by account number: " + accountNumber));
    }

    /**
     * Retrieves all accounts within a specified date range, ensuring they are not marked as deleted.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return a list of accounts within the date range
     * @throws BusinessException if the start date or end date is null
     * @throws BusinessException if the start date is after the end date
     */
    public List<Account> getAccountByOpeningDateRange(LocalDate startDate, LocalDate endDate){
        if (startDate == null || endDate == null) {
            throw new BusinessException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date");
        }
        return accountRepository.findByOpeningDateBetweenAndDeletedFalse(startDate, endDate);
    }

    /**
     * Retrieves all accounts with a specified status, ensuring they are not marked as deleted.
     *
     * @param status the status of the accounts to retrieve
     * @return a list of accounts with the specified status
     * @throws BusinessException if the status is null
     */
    public List<Account> getAccountByStatus(AccountStatus status){
        if(status == null) {
            throw new BusinessException("Account status cannot be null");
        }
        return accountRepository.findByStatusAndDeletedFalse(status);
    }

    /**
     * Retrieves an account by its ID, ensuring it is marked as deleted.
     *
     * @param id the ID of the account to retrieve
     * @return the found account
     * @throws BusinessException if the ID is null or invalid
     * @throws ResourceNotFoundException if no deleted account is found with the given ID
     */
    public Account getByIdAndDeletedTrue(Long id) {
        if(id == null || id <= 0) {
            throw new BusinessException("Account ID cannot be null.");
        }
        Optional<Account> account = accountRepository.findByIdAndDeletedTrue(id);
        return account.orElseThrow(() -> new ResourceNotFoundException("Deleted account not found"));
    }

    /**
     * Retrieves an account by its account number, ensuring it is marked as deleted.
     *
     * @param accountNumber the account number of the account to retrieve
     * @return the found account
     * @throws BusinessException if the account number is null or empty
     * @throws ResourceNotFoundException if no deleted account is found with the given account number
     */
    public Account getByAccountNumberAndDeletedTrue(String accountNumber) {
        if(accountNumber == null || accountNumber.isEmpty()) {
            throw new BusinessException("Account number cannot be null or empty.");
        }
        Optional<Account> account = accountRepository.findByAccountNumberAndDeletedTrue(accountNumber);
        return account.orElseThrow(() -> new ResourceNotFoundException("Deleted account not found by account number: " + accountNumber));
    }

    /**
     * Retrieves all accounts within a specified date range, ensuring they are marked as deleted.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return a list of deleted accounts within the date range
     * @throws BusinessException if the start date or end date is null
     * @throws BusinessException if the start date is after the end date
     */
    public List<Account> getAccountByOpeningDateRangeAndDeletedTrue(LocalDate startDate, LocalDate endDate){
        if (startDate == null || endDate == null) {
            throw new BusinessException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date");
        }
        return accountRepository.findByOpeningDateBetweenAndDeletedTrue(startDate, endDate);
    }

    /**
     * Retrieves all accounts with a specified status, ensuring they are marked as deleted.
     *
     * @param status the status of the accounts to retrieve
     * @return a list of deleted accounts with the specified status
     * @throws BusinessException if the status is null
     */
    public List<Account> getAccountByStatusAndDeletedTrue(AccountStatus status){
        if(status == null) {
            throw new BusinessException("Account status cannot be null");
        }
        return accountRepository.findByStatusAndDeletedTrue(status);
    }

    // AUXILIARY

    /**
     * Validates the credit amount for an account, ensuring it is greater than zero.
     *
     * @param account the account to validate
     * @param credit the credit amount to validate
     * @return the validated account
     * @throws InvalidTransferException if the credit amount is less than or equal to zero
     */
    private Account validateCredit(Account account, BigDecimal credit) {

        if (credit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException("Credit amount must be greater than zero.");
        }
        return account;
    }

    /**
     * Validates the debit amount for an account, ensuring it is greater than zero and does not exceed the account's balance.
     *
     * @param account the account to validate
     * @param debit the debit amount to validate
     * @return the validated account
     * @throws InvalidTransferException if the debit amount is less than or equal to zero
     * @throws InsufficientBalanceException if the debit amount exceeds the account's balance
     */
    private Account validateDebit(Account account, BigDecimal debit) {
        if (debit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException("Debit amount must be greater than zero.");
        }
        if (account.getBalance().compareTo(debit) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for the debit operation.");
        }
        return account;
    }

}
