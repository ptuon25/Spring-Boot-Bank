package com.tuon.tbank.service.account;

import com.tuon.tbank.dto.account.AccountBalanceResponse;
import com.tuon.tbank.entity.account.CheckingAccount;
import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.enums.account.AccountStatus;
import com.tuon.tbank.exception.serviceException.BusinessException;
import com.tuon.tbank.exception.serviceException.DatabaseException;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.account.CheckingAccountRepository;
import com.tuon.tbank.repository.person.ClientRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing checking accounts.
 * This class provides methods to perform operations related to checking accounts, such as retrieval and validation.
 */
@Service
public class CheckingAccountService {

    private final CheckingAccountRepository checkingAccountRepository;
    private final AccountNumberGenerator numberGenerator;
    private final ClientRepository clientRepository;

    /**
     * Constructs a new CheckingAccountService with the specified dependencies.
     *
     * @param checkingAccountRepository the repository for accessing checking account data
     * @param numberGenerator           the utility for generating unique account numbers
     * @param clientRepository          the repository for accessing client data
     */
    public CheckingAccountService(CheckingAccountRepository checkingAccountRepository, AccountNumberGenerator numberGenerator, ClientRepository clientRepository) {
        this.checkingAccountRepository = checkingAccountRepository;
        this.numberGenerator = numberGenerator;
        this.clientRepository = clientRepository;
    }

    /**
     * Retrieves all checking accounts from the repository.
     *
     * @return a list of all CheckingAccount entities
     */
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public List<CheckingAccount> getAllCheckingAccounts() {
        return checkingAccountRepository.findAll();
    }

    /**
     * Retrieves a checking account by the owner's ID.
     *
     * <p>This method checks if the provided owner ID is valid and then attempts to find a checking account associated with that owner.
     * If no account is found, a ResourceNotFoundException is thrown.</p>
     *
     * @param ownerId the ID of the owner of the checking account
     * @return the found CheckingAccount
     * @throws BusinessException         if the owner ID is null or invalid
     * @throws ResourceNotFoundException if no checking account is found for the given owner ID
     */
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public CheckingAccount getCheckingAccountByOwnerId(Long ownerId) {
        if (ownerId == null || ownerId <= 0) {
            throw new BusinessException("Owner ID cannot be null or zero");
        }
        Optional<CheckingAccount> checkingAccount = checkingAccountRepository.findByOwnerIdAndDeletedFalse(ownerId);
        return checkingAccount.orElseThrow(() -> new ResourceNotFoundException("Checking account not found"));
    }

    /**
     * Retrieves a checking account by its ID and the owner's ID.
     *
     * <p>This method checks if the provided account ID and owner ID are valid and then attempts to find a checking account
     * that matches both IDs. If no account is found, a ResourceNotFoundException is thrown.</p>
     *
     * @param accountId the ID of the checking account
     * @param ownerId   the ID of the owner of the checking account
     * @return the found CheckingAccount
     * @throws BusinessException         if either the account ID or owner ID is null or invalid
     * @throws ResourceNotFoundException if no checking account is found for the given IDs
     */
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public CheckingAccount getCheckingAccountByIdAndOwnerId(Long accountId, Long ownerId) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        if (ownerId == null || ownerId <= 0) {
            throw new BusinessException("Owner ID cannot be null or zero");
        }
        Optional<CheckingAccount> checkingAccount = checkingAccountRepository.findByIdAndOwnerIdAndDeletedFalse(accountId, ownerId);
        return checkingAccount.orElseThrow(() -> new ResourceNotFoundException("Checking account not found"));
    }

    /**
     * Retrieves a checking account by its account number.
     *
     * <p>This method checks if the provided account number is valid and then attempts to find a checking account
     * that matches the account number. If no account is found, a ResourceNotFoundException is thrown.</p>
     *
     * @param accountNumber the account number of the checking account
     * @return the found CheckingAccount
     * @throws BusinessException         if the account number is null or empty
     * @throws ResourceNotFoundException if no checking account is found for the given account number
     */
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public CheckingAccount getCheckingAccountByAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            throw new BusinessException("Account number cannot be null or empty");
        }
        String normalizedAccount = buildCheckingAccountNumber(accountNumber);
        Optional<CheckingAccount> checkingAccount = checkingAccountRepository.findByAccountNumberAndDeletedFalse(normalizedAccount);
        return checkingAccount.orElseThrow(() -> new ResourceNotFoundException("Checking account not found"));
    }

    /**
     * Retrieves all active checking accounts.
     *
     * @return a list of all active checking accounts
     */
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public List<CheckingAccount> getAllActiveCheckingAccounts() {
        return checkingAccountRepository.findAllByDeletedFalse();
    }

    /**
     * Retrieves a checking account by its ID, ensuring it is not marked as deleted.
     *
     * @return a list of all deleted checking accounts
     */
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public List<CheckingAccount> getAllDeletedCheckingAccounts() {
        return checkingAccountRepository.findAllByDeletedTrue();
    }

    /**
     * Retrieves a deleted checking account by its ID.
     *
     * <p>This method checks if the provided account ID is valid and then attempts to find a checking account
     * that is marked as deleted. If no account is found, a ResourceNotFoundException is thrown.</p>
     *
     * @param accountId the ID of the checking account
     * @return the found CheckingAccount
     * @throws BusinessException         if the account ID is null or invalid
     * @throws ResourceNotFoundException if no deleted checking account is found for the given ID
     */
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public CheckingAccount getDeletedCheckingAccountById (Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        Optional<CheckingAccount> checkingAccount = checkingAccountRepository.findByIdAndDeletedTrue(accountId);
        return checkingAccount.orElseThrow(() -> new ResourceNotFoundException("Deleted checking account not found"));
    }

    /**
     * Retrieves the balance of a checking account by its ID.
     *
     * <p>This method checks if the provided account ID is valid and then attempts to find a checking account
     * that matches the ID. If no account is found, a ResourceNotFoundException is thrown.</p>
     *
     * @param accountId the ID of the checking account
     * @return an AccountBalanceResponse containing the account's balance information
     * @throws BusinessException         if the account ID is null or invalid
     * @throws ResourceNotFoundException if no checking account is found for the given ID
     */
    @PreAuthorize("@accountAuthorization.canAccessCheckingAccount(#accountId, authentication)")
    public AccountBalanceResponse getCheckingAccountBalance(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        CheckingAccount checkingAccount = checkingAccountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Checking account not found"));
        return new AccountBalanceResponse(checkingAccount.getId(), checkingAccount.getAccountNumber(), checkingAccount.getBalance());
    }

    /**
     * Registers a new checking account for a client.
     *
     * <p>This method validates the provided balance, opening date, and owner before creating a new checking account.
     * It ensures that the balance is not null or negative, the opening date is not in the future, and the owner is valid.</p>
     *
     * @param balance     the initial balance of the checking account
     * @param openingDate the date when the account was opened
     * @param owner       the client who owns the checking account
     * @return the newly created CheckingAccount
     * @throws BusinessException if any validation fails
     */
    @PreAuthorize("@accountAuthorization.canOpenAccountFor(#checkingAccount?.owner?.id, authentication)")
    public CheckingAccount registerCheckingAccount(BigDecimal balance,
                                                   LocalDate openingDate,
                                                   Client owner) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Checking account balance cannot be null or negative");
        }
        if (openingDate == null) {
            throw new BusinessException("Checking account opening date cannot be null");
        }
        if (openingDate.isAfter(LocalDate.now())) {
            throw new BusinessException("Checking account opening date cannot be in the future");
        }
        if (owner == null) {
            throw new BusinessException("Checking account owner cannot be null");
        }
        owner = getActiveOwnerById(owner.getId());
        CheckingAccount checkingAccount = new CheckingAccount(null, null, balance, openingDate, AccountStatus.ACTIVE, owner);
        return createCheckingAccount(checkingAccount);
    }

    /**
     * Creates a new checking account in the system.
     *
     * <p>This method validates the provided CheckingAccount object, generates a unique account number,
     * and saves it to the repository. It ensures that the account number is not null or empty.</p>
     *
     * @param checkingAccount the CheckingAccount object to create
     * @return the newly created CheckingAccount
     * @throws BusinessException if any validation fails or if the account number is null or empty
     * @throws DatabaseException if there is a database constraint violation during saving
     */
    @PreAuthorize("@accountAuthorization.canOpenAccountFor(#checkingAccount?.owner?.id, authentication)")
    public CheckingAccount createCheckingAccount (CheckingAccount checkingAccount){
        validateForCreate(checkingAccount);
        checkingAccount.setAccountNumber(generateUniqueCheckingAccountNumber());
        if(checkingAccount.getAccountNumber() == null || checkingAccount.getAccountNumber().isEmpty()){
            throw new BusinessException("Checking account number cannot be null or empty");
        }
        try {
            return checkingAccountRepository.saveAndFlush(checkingAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not create checking account due to a database constraint.");
        }
    }

    /**
     * Blocks a checking account by its ID and account number.
     *
     * <p>This method checks if the provided account ID and account number are valid, retrieves the corresponding checking account,
     * and updates its status to BLOCKED. It ensures that the account is not already blocked or suspended.</p>
     *
     * @param accountId     the ID of the checking account to block
     * @param accountNumber the account number of the checking account to block
     * @return the updated CheckingAccount with status BLOCKED
     * @throws BusinessException         if any validation fails or if the account is already blocked or suspended
     * @throws ResourceNotFoundException if no checking account is found for the given IDs
     * @throws DatabaseException         if there is a database constraint violation during saving
     */
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public CheckingAccount blockCheckingAccount(Long accountId, String accountNumber) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        if (accountNumber == null || accountNumber.isEmpty()) {
            throw new BusinessException("Account number cannot be null or empty");
        }
        String normalizedAccount = buildCheckingAccountNumber(accountNumber);
        CheckingAccount checkingAccount = checkingAccountRepository.findByAccountNumberAndDeletedFalse(normalizedAccount)
                .orElseThrow(() -> new ResourceNotFoundException("Checking account not found"));
        CheckingAccount accountById = checkingAccountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Checking account not found"));
        if (!checkingAccount.getId().equals(accountById.getId())) {
            throw new BusinessException("Account number does not match the provided account ID.");
        }
        if (checkingAccount.getStatus() == AccountStatus.BLOCKED) {
            throw new BusinessException("Checking account is already blocked.");
        }
        if (checkingAccount.getStatus() == AccountStatus.SUSPENDED) {
            throw new BusinessException("Cannot block a suspended checking account.");
        }
        checkingAccount.setStatus(AccountStatus.BLOCKED);
        try {
            return checkingAccountRepository.saveAndFlush(checkingAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not block checking account due to a database constraint.");
        }
    }

    /**
     * Suspends a checking account by its ID and account number.
     *
     * <p>This method checks if the provided account ID and account number are valid, retrieves the corresponding checking account,
     * and updates its status to SUSPENDED. It ensures that the account is not already suspended.</p>
     *
     * @param accountId     the ID of the checking account to suspend
     * @param accountNumber the account number of the checking account to suspend
     * @return the updated CheckingAccount with status SUSPENDED
     * @throws BusinessException         if any validation fails or if the account is already suspended
     * @throws ResourceNotFoundException if no checking account is found for the given IDs
     * @throws DatabaseException         if there is a database constraint violation during saving
     */
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public CheckingAccount suspendCheckingAccount(Long accountId, String accountNumber) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        if (accountNumber == null || accountNumber.isEmpty()) {
            throw new BusinessException("Account number cannot be null or empty");
        }
        String normalizedAccount = buildCheckingAccountNumber(accountNumber);
        CheckingAccount checkingAccount = checkingAccountRepository.findByAccountNumberAndDeletedFalse(normalizedAccount)
                .orElseThrow(() -> new ResourceNotFoundException("Checking account not found"));
        CheckingAccount accountById = checkingAccountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Checking account not found"));
        if (!checkingAccount.getId().equals(accountById.getId())) {
            throw new BusinessException("Account number does not match the provided account ID.");
        }
        if (checkingAccount.getStatus() == AccountStatus.SUSPENDED) {
            throw new BusinessException("Checking account is already suspended.");
        }
        checkingAccount.setStatus(AccountStatus.SUSPENDED);
        try {
            return checkingAccountRepository.saveAndFlush(checkingAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not suspend checking account due to a database constraint.");
        }
    }

    /**
     * Reactivates a checking account by its ID and account number.
     *
     * <p>This method checks if the provided account ID and account number are valid, retrieves the corresponding checking account,
     * and updates its status to ACTIVE. It ensures that the account is not already active.</p>
     *
     * @param accountId     the ID of the checking account to reactivate
     * @param accountNumber the account number of the checking account to reactivate
     * @return the updated CheckingAccount with status ACTIVE
     * @throws BusinessException         if any validation fails or if the account is already active
     * @throws ResourceNotFoundException if no checking account is found for the given IDs
     * @throws DatabaseException         if there is a database constraint violation during saving
     */
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public CheckingAccount reactivateCheckingAccount(Long accountId, String accountNumber) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        if (accountNumber == null || accountNumber.isEmpty()) {
            throw new BusinessException("Account number cannot be null or empty");
        }
        String normalizedAccount = buildCheckingAccountNumber(accountNumber);
        CheckingAccount checkingAccount = checkingAccountRepository.findByAccountNumberAndDeletedFalse(normalizedAccount)
                .orElseThrow(() -> new ResourceNotFoundException("Checking account not found"));
        CheckingAccount accountById = checkingAccountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Checking account not found"));
        if (!checkingAccount.getId().equals(accountById.getId())) {
            throw new BusinessException("Account number does not match the provided account ID.");
        }
        if (checkingAccount.getStatus() == AccountStatus.ACTIVE) {
            throw new BusinessException("Checking account is already active.");
        }
        checkingAccount.setStatus(AccountStatus.ACTIVE);
        try {
            return checkingAccountRepository.saveAndFlush(checkingAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not reactivate checking account due to a database constraint.");
        }
    }

    /**
     * Updates the owner of a checking account.
     *
     * <p>This method checks if the provided account ID and new owner ID are valid, retrieves the corresponding checking account,
     * and updates its owner to the new client. It ensures that the new owner is not already the current owner of the account.</p>
     *
     * @param accountId   the ID of the checking account to update
     * @param newOwnerId  the ID of the new owner for the checking account
     * @return the updated CheckingAccount with the new owner
     * @throws BusinessException         if any validation fails or if the new owner is already the current owner
     * @throws ResourceNotFoundException if no checking account or client is found for the given IDs
     * @throws DatabaseException         if there is a database constraint violation during saving
     */
    @PreAuthorize("@accountAuthorization.canAdministerAccount(authentication)")
    public CheckingAccount updateCheckingAccountOwner(Long accountId, Long newOwnerId) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        if (newOwnerId == null || newOwnerId <= 0) {
            throw new BusinessException("New owner ID cannot be null or zero");
        }
        CheckingAccount checkingAccount = checkingAccountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Checking account not found"));
        Client newOwner = getActiveOwnerById(newOwnerId);
        if (checkingAccount.getOwner() != null
        && checkingAccount.getOwner().getId() !=null
        && checkingAccount.getOwner().getId().equals(newOwner.getId())) {
            throw new BusinessException("New owner is already the owner of the account.");
        }
        checkingAccount.setOwner(newOwner);
        try{
            return checkingAccountRepository.saveAndFlush(checkingAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not update checking account owner due to a database constraint.");
        }
    }

    /**
     * Deletes a checking account by its ID.
     *
     * <p>This method checks if the provided account ID is valid, retrieves the corresponding checking account,
     * and marks it as deleted. It ensures that the account is not already marked as deleted.</p>
     *
     * @param accountId the ID of the checking account to delete
     * @throws BusinessException         if the account ID is null or invalid
     * @throws ResourceNotFoundException if no checking account is found for the given ID
     * @throws DatabaseException         if there is a database constraint violation during saving
     */
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public void deleteCheckingAccount(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        CheckingAccount checkingAccount = checkingAccountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Checking account not found"));
        checkingAccount.setDeleted(true);
        try {
            checkingAccountRepository.saveAndFlush(checkingAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not delete checking account due to a database constraint.");
        }
    }

    /**
     * Restores a previously deleted checking account by its ID.
     *
     * <p>This method checks if the provided account ID is valid, retrieves the corresponding checking account that is marked as deleted,
     * and marks it as active again. It ensures that the account is not already active.</p>
     *
     * @param accountId the ID of the checking account to restore
     * @return the restored CheckingAccount
     * @throws BusinessException         if the account ID is null or invalid
     * @throws ResourceNotFoundException if no deleted checking account is found for the given ID
     * @throws DatabaseException         if there is a database constraint violation during saving
     */
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public CheckingAccount restoreCheckingAccount(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        CheckingAccount checkingAccount = checkingAccountRepository.findByIdAndDeletedTrue(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted checking account not found"));
        checkingAccount.setDeleted(false);
        try {
            return checkingAccountRepository.saveAndFlush(checkingAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not restore checking account due to a database constraint.");
        }
    }

    // AUXILIARY

    /**
     * Builds a valid business account number by ensuring it has the correct prefix and format.
     *
     * @param number the raw account number input
     * @return a formatted business account number with the "CA-" prefix
     * @throws BusinessException if the input number is null or does not contain exactly 8 digits
     */
    private String buildCheckingAccountNumber(String number) {
        if (number == null) {
            throw new BusinessException("Account number must contain exactly 8 digits");
        }

        String numericNumber = number.startsWith("CA-")
                ? number.substring(3)
                : number;

        if (!numericNumber.matches("\\d{8}")) {
            throw new BusinessException("Account number must contain exactly 8 digits");
        }

        return "CA-" + numericNumber;
    }

    /**
     * Retrieves an active client by its ID, ensuring it is not marked as deleted.
     *
     * @param id the ID of the client to retrieve
     * @return the found client
     * @throws BusinessException         if the ID is null or invalid
     * @throws ResourceNotFoundException if no active client is found with the given ID
     */
    private Client getActiveOwnerById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("Client ID cannot be null or zero");
        }
        Optional<Client> client = clientRepository.findByIdAndDeletedFalse(id);
        return client.orElseThrow(() -> new ResourceNotFoundException("Client not found"));
    }

    /**
     * Generates a unique account number for a checking account.
     *
     * <p>This method generates a unique account number by prefixing the generated number with "CA".</p>
     *
     * @return a unique checking account number
     */
    private String generateUniqueCheckingAccountNumber() {
        return numberGenerator.generateUniqueAccountNumber("CA");
    }

    /**
     * Validates the provided CheckingAccount object for creation.
     *
     * <p>This method checks for null values, negative balances, and ensures that the account status is ACTIVE.</p>
     *
     * @param checkingAccount the CheckingAccount object to validate
     * @throws BusinessException if any validation fails
     */
    private void validateForCreate(CheckingAccount checkingAccount) {
        if (checkingAccount == null) {
            throw new BusinessException("Checking account cannot be null");
        }
        if (checkingAccount.getBalance() == null || checkingAccount.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Checking account balance cannot be null or negative");
        }
        if (checkingAccount.getOpeningDate() == null) {
            throw new BusinessException("Checking account opening date cannot be null");
        }
        if (checkingAccount.getStatus() == null) {
            throw new BusinessException("Checking account status cannot be null");
        }
        if (checkingAccount.getStatus() != AccountStatus.ACTIVE){
            throw new BusinessException("Checking account status must be ACTIVE");
        }
        checkingAccount.setOwner(getActiveOwnerById(checkingAccount.getOwner().getId()));
    }
}