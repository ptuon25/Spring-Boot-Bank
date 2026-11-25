package com.tuon.tbank.service.account;

import com.tuon.tbank.dto.account.AccountBalanceResponse;
import com.tuon.tbank.entity.account.JointAccount;
import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.enums.account.AccountStatus;
import com.tuon.tbank.enums.person.ClientStatus;
import com.tuon.tbank.exception.serviceException.BusinessException;
import com.tuon.tbank.exception.serviceException.DatabaseException;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.account.JointAccountRepository;
import com.tuon.tbank.repository.person.ClientRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Service class for managing joint accounts.
 */
@Service
public class JointAccountService {

    private final JointAccountRepository jointAccountRepository;
    private final AccountNumberGenerator numberGenerator;
    private final ClientRepository clientRepository;

    public JointAccountService(JointAccountRepository jointAccountRepository, AccountNumberGenerator numberGenerator, ClientRepository clientRepository) {
        this.jointAccountRepository = jointAccountRepository;
        this.numberGenerator = numberGenerator;
        this.clientRepository = clientRepository;
    }

    /**
     * Retrieves all joint accounts that are not marked as deleted.
     *
     * @return a list of joint accounts
     */
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public List<JointAccount> getAllJointAccounts() {
        return jointAccountRepository.findAllByDeletedFalse();
    }

    /**
     * Retrieves a joint account by its ID, ensuring it is not marked as deleted.
     *
     * @param id the ID of the joint account
     * @return the found joint account
     * @throws ResourceNotFoundException if the joint account is not found
     */
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public JointAccount getJointAccountById(Long id) {
        Optional<JointAccount> jointAccount = jointAccountRepository.findByIdAndDeletedFalse(id);
        return jointAccount.orElseThrow(() -> new ResourceNotFoundException("Joint Account not found with id: " + id));
    }

    /**
     * Retrieves a joint account by its account number, ensuring it is not marked as deleted.
     *
     * @param accountNumber the account number of the joint account
     * @return the found joint account
     * @throws ResourceNotFoundException if the joint account is not found
     */
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public JointAccount getJointAccountByAccountNumber(String accountNumber) {
        Optional<JointAccount> jointAccount = jointAccountRepository.findByAccountNumberAndDeletedFalse(accountNumber);
        return jointAccount.orElseThrow(() -> new ResourceNotFoundException("Joint Account not found with account number: " + accountNumber));
    }

    /**
     * Retrieves a list of joint accounts that have owners with the specified IDs, ensuring they are not marked as deleted.
     *
     * @param ownerIds a collection of owner IDs to search for
     * @return a list of joint accounts with the specified owner IDs
     */
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public List<JointAccount> getJointAccountsByOwnersId(Collection<Long> ownerIds) {
        return jointAccountRepository.findByOwnersIdInAndDeletedFalse(ownerIds);
    }

    /**
     * Retrieves a list of joint accounts that have a specific owner ID, ensuring they are not marked as deleted.
     *
     * @param ownerId the ID of the owner to search for
     * @return a list of joint accounts with the specified owner ID
     */
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public List<JointAccount> getJointAccountsByOwnerId(Long ownerId) {
        if (!clientRepository.existsById(ownerId)) {
            throw new ResourceNotFoundException("Client not found with id: " + ownerId);
        }
        return jointAccountRepository.findByOwnerId(ownerId);
    }

    /**
     * Retrieves a joint account by its ID and a specific owner ID, ensuring the account is not marked as deleted.
     *
     * @param accountId the ID of the joint account
     * @param ownerId   the ID of the owner
     * @return the found joint account
     * @throws ResourceNotFoundException if the joint account is not found
     */
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public JointAccount getJointAccountByIdAndOwnerId(Long accountId, Long ownerId) {
        Optional<JointAccount> jointAccount = jointAccountRepository.findByIdAndOwners_IdAndDeletedFalse(accountId, ownerId);
        return jointAccount.orElseThrow(() -> new ResourceNotFoundException("Joint Account not found with id: " + accountId + " and owner id: " + ownerId));
    }

    /**
     * Retrieves the balance of a joint account by its ID, ensuring the account is not marked as deleted.
     *
     * @param accountId the ID of the joint account
     * @return an {@link AccountBalanceResponse} containing the account's balance information
     * @throws ResourceNotFoundException if the joint account is not found
     */
    @PreAuthorize("@accountAuthorization.canAccessJointAccount(#accountId, authentication)")
    public AccountBalanceResponse getJointAccountBalance(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        JointAccount jointAccount = jointAccountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Joint Account not found with id: " + accountId));
        return new AccountBalanceResponse(
                jointAccount.getId(),
                jointAccount.getAccountNumber(),
                jointAccount.getBalance()
        );
    }

    /**
     * Retrieves a list of joint accounts that are marked as deleted.
     *
     * @return a list of deleted joint accounts
     */
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public List<JointAccount> getAllDeletedJointAccounts() {
        return jointAccountRepository.findAllByDeletedTrue();
    }

    /**
     * Retrieves a joint account by its ID, ensuring it is marked as deleted.
     *
     * @param id the ID of the joint account
     * @return the found deleted joint account
     * @throws ResourceNotFoundException if the joint account is not found
     */
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public JointAccount getDeletedJointAccountById(Long id) {
        Optional<JointAccount> jointAccount = jointAccountRepository.findByIdAndDeletedTrue(id);
        return jointAccount.orElseThrow(() -> new ResourceNotFoundException("Deleted Joint Account not found with id: " + id));
    }

    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public JointAccount getDeletedJointAccountByAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            throw new BusinessException("Account number cannot be null or empty");
        }
        Optional<JointAccount> jointAccount = jointAccountRepository.findByAccountNumberAndDeletedFalse(accountNumber);
        return jointAccount.orElseThrow(() -> new ResourceNotFoundException("Deleted Joint Account not found with account number: " + accountNumber));
    }

    /**
     * Registers a new joint account with the specified owners.
     *
     * @param owner1Id the ID of the first owner
     * @param owner2Id the ID of the second owner
     * @return the created joint account
     * @throws BusinessException if the number of owners is not exactly two
     */
    @PreAuthorize("@accountAuthorization.canOpenJointAccountFor(#ownersIds, authentication)")
    public JointAccount registerJointAccount(Long owner1Id, Long owner2Id) {
        if (owner1Id == null || owner2Id == null) {
            throw new BusinessException("Owner IDs cannot be null");
        }
        Set<Long> ownersIds = Set.of(owner1Id, owner2Id);
        if (ownersIds == null || ownersIds.size() != 2) {
            throw new BusinessException("Joint account must have exactly two owners");
        }
        Set<Client> owners = new HashSet<>();
        for (Long ownerId : ownersIds) {
            owners.add(getActiveOwnersById(ownerId));
        }
        BigDecimal balance = BigDecimal.ZERO; // Set initial balance to zero
        JointAccount jointAccount = new JointAccount(null, null, balance, LocalDate.now(), AccountStatus.ACTIVE, owners);
        return createJointAccount(jointAccount);
    }

    /**
     * Creates a new joint account in the system.
     *
     * @param jointAccount the joint account to create
     * @return the created joint account
     * @throws BusinessException if the joint account is invalid or if the account number is null or empty
     * @throws DatabaseException if there is a database constraint violation during creation
     */
    @PreAuthorize("@accountAuthorization.canOpenJointAccountFor(#ownersIds, authentication)")
    public JointAccount createJointAccount(@P("jointAccount") JointAccount jointAccount) {
        validateForCreate(jointAccount);
        jointAccount.setAccountNumber(generateUniqueJointAccountNumber());
        if (jointAccount.getAccountNumber() == null || jointAccount.getAccountNumber().isEmpty()) {
            throw new BusinessException("Account number cannot be null or empty");
        }
        try {
            return jointAccountRepository.saveAndFlush(jointAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not create checking account due to a database constraint.");
        }
    }

    /**
     * Blocks a joint account by its ID and account number, ensuring the account is not already blocked or suspended.
     *
     * @param accountId     the ID of the joint account
     * @param accountNumber the account number of the joint account
     * @return the updated joint account with status set to BLOCKED
     * @throws BusinessException         if the account is already blocked or suspended, or if the ID and number do not match
     * @throws ResourceNotFoundException if the joint account is not found
     * @throws DatabaseException         if there is a database constraint violation during the update
     */
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public JointAccount blockJointAccount(Long accountId, String accountNumber) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        if (accountNumber == null || accountNumber.isEmpty()) {
            throw new BusinessException("Account number cannot be null or empty");
        }
        String normalizedAccount = buildJointAccountNumber(accountNumber);
        JointAccount jointAccount = jointAccountRepository.findByAccountNumberAndDeletedFalse(normalizedAccount)
                .orElseThrow(() -> new ResourceNotFoundException("Joint Account not found with account number: " + normalizedAccount));
        JointAccount jointAccountById = jointAccountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Joint Account not found with id: " + accountId));
        if (!jointAccount.getId().equals(jointAccountById.getId())) {
            throw new BusinessException("Account ID and account number do not match");
        }
        if (jointAccount.getStatus() == AccountStatus.BLOCKED) {
            throw new BusinessException("Joint account is already blocked");
        }
        if (jointAccount.getStatus() == AccountStatus.SUSPENDED) {
            throw new BusinessException("Joint account is suspended and cannot be blocked");
        }
        jointAccount.setStatus(AccountStatus.BLOCKED);
        try {
            return jointAccountRepository.saveAndFlush(jointAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not block joint account due to a database constraint.");
        }
    }

    /**
     * Suspends a joint account by its ID and account number, ensuring the account is not already suspended.
     *
     * @param accountId     the ID of the joint account
     * @param accountNumber the account number of the joint account
     * @return the updated joint account with status set to SUSPENDED
     * @throws BusinessException         if the account is already suspended, or if the ID and number do not match
     * @throws ResourceNotFoundException if the joint account is not found
     * @throws DatabaseException         if there is a database constraint violation during the update
     */
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public JointAccount suspendJointAccount(Long accountId, String accountNumber) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        if (accountNumber == null || accountNumber.isEmpty()) {
            throw new BusinessException("Account number cannot be null or empty");
        }
        String normalizedAccount = buildJointAccountNumber(accountNumber);
        JointAccount jointAccount = jointAccountRepository.findByAccountNumberAndDeletedFalse(normalizedAccount)
                .orElseThrow(() -> new ResourceNotFoundException("Joint Account not found with account number: " + normalizedAccount));
        JointAccount jointAccountById = jointAccountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Joint Account not found with id: " + accountId));
        if (!jointAccount.getId().equals(jointAccountById.getId())) {
            throw new BusinessException("Account ID and account number do not match");
        }
        if (jointAccount.getStatus() == AccountStatus.SUSPENDED) {
            throw new BusinessException("Joint account is already suspended");
        }
        if (jointAccount.getStatus() == AccountStatus.BLOCKED) {
            throw new BusinessException("Joint account is blocked and cannot be suspended");
        }
        jointAccount.setStatus(AccountStatus.SUSPENDED);
        try {
            return jointAccountRepository.saveAndFlush(jointAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not suspend joint account due to a database constraint.");
        }
    }

    /**
     * Reactivates a joint account by its ID and account number, ensuring the account is not already active.
     *
     * @param accountId     the ID of the joint account
     * @param accountNumber the account number of the joint account
     * @return the updated joint account with status set to ACTIVE
     * @throws BusinessException         if the account is already active, or if the ID and number do not match
     * @throws ResourceNotFoundException if the joint account is not found
     * @throws DatabaseException         if there is a database constraint violation during the update
     */
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public JointAccount reactivateJointAccount(Long accountId, String accountNumber) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        if (accountNumber == null || accountNumber.isEmpty()) {
            throw new BusinessException("Account number cannot be null or empty");
        }
        String normalizedAccount = buildJointAccountNumber(accountNumber);
        JointAccount jointAccount = jointAccountRepository.findByAccountNumberAndDeletedFalse(normalizedAccount)
                .orElseThrow(() -> new ResourceNotFoundException("Joint Account not found with account number: " + normalizedAccount));
        JointAccount jointAccountById = jointAccountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Joint Account not found with id: " + accountId));
        if (!jointAccount.getId().equals(jointAccountById.getId())) {
            throw new BusinessException("Account ID and account number do not match");
        }
        if (jointAccount.getStatus() == AccountStatus.ACTIVE) {
            throw new BusinessException("Joint account is already active");
        }
        jointAccount.setStatus(AccountStatus.ACTIVE);
        try {
            return jointAccountRepository.saveAndFlush(jointAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not reactivate joint account due to a database constraint.");
        }
    }

    /**
     * Updates the owners of a joint account.
     *
     * @param accountId    the ID of the joint account
     * @param newOwnersIds the IDs of the new owners
     * @return the updated joint account
     * @throws BusinessException         if the account is not found or if the number of owners is not exactly two
     * @throws ResourceNotFoundException if the joint account is not found
     * @throws DatabaseException         if there is a database constraint violation during the update
     */
    @PreAuthorize("@accountAuthorization.canAdministerAccount(authentication)")
    public JointAccount updateJointAccountOwners(Long accountId, Collection<Long> newOwnersIds) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        if (newOwnersIds == null || newOwnersIds.size() != 2) {
            throw new BusinessException("Joint account must have exactly two owners");
        }
        JointAccount jointAccount = jointAccountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Joint account not found"));
        Set<Client> newOwners = new HashSet<>();
        for (Long ownerId : newOwnersIds) {
            newOwners.add(getActiveOwnersById(ownerId));
        }
        if (jointAccount.getOwners() != null
                && jointAccount.getOwners().size() == 2
                && jointAccount.getOwners().containsAll(newOwners)) {
            throw new BusinessException("The new owners are the same as the current owners");
        }
        try {
            return jointAccountRepository.saveAndFlush(jointAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not update joint account owners due to a database constraint.");
        }
    }

    /**
     * Deletes a joint account by its ID, ensuring the account has a balance of zero before deletion.
     *
     * @param accountId the ID of the joint account to delete
     * @throws BusinessException         if the account has a non-zero balance or if the account ID is invalid
     * @throws ResourceNotFoundException if the joint account is not found
     * @throws DatabaseException         if there is a database constraint violation during deletion
     */
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public void deleteJointAccount(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        JointAccount jointAccount = jointAccountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Joint account not found"));
        if (jointAccount.getBalance() == null || jointAccount.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("Joint account must have a balance of zero to be deleted");
        }
        jointAccount.setDeleted(true);
        try {
            jointAccountRepository.saveAndFlush(jointAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not delete joint account due to a database constraint.");
        }
    }

    /**
     * Restores a deleted joint account by its ID.
     *
     * @param accountId the ID of the joint account to restore
     * @return the restored joint account
     * @throws BusinessException         if the account ID is invalid
     * @throws ResourceNotFoundException if the deleted joint account is not found
     * @throws DatabaseException         if there is a database constraint violation during restoration
     */
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public JointAccount restoreJointAccount(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        JointAccount jointAccount = jointAccountRepository.findByIdAndDeletedTrue(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted joint account not found"));
        jointAccount.setDeleted(false);
        try {
            return jointAccountRepository.saveAndFlush(jointAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not restore joint account due to a database constraint.");
        }
    }

    // AUXILIARY

    /**
     * Builds a valid joint account number by ensuring it has the correct prefix and format.
     *
     * @param number the raw account number input
     * @return a formatted business account number with the "JA-" prefix
     * @throws BusinessException if the input number is null or does not contain exactly 8 digits
     */
    private String buildJointAccountNumber(String number) {
        if (number == null) {
            throw new BusinessException("Account number must contain exactly 8 digits");
        }

        String numericNumber = number.startsWith("JA-")
                ? number.substring(3)
                : number;

        if (!numericNumber.matches("\\d{8}")) {
            throw new BusinessException("Account number must contain exactly 8 digits");
        }

        return "JA-" + numericNumber;
    }

    /**
     * Retrieves an active owner by their ID, ensuring the owner exists and is in an ACTIVE status.
     *
     * @param id the ID of the owner
     * @return the found active owner
     * @throws BusinessException         if the client ID is null or zero, or if the owner is not ACTIVE
     * @throws ResourceNotFoundException if the owner is not found
     */
    public Client getActiveOwnersById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("Client ID cannot be null or zero");
        }
        Optional<Client> client = clientRepository.findByIdAndDeletedFalse(id);
        Client owner = client.orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        if (owner.getStatus() != ClientStatus.ACTIVE) {
            throw new BusinessException("Client owner must be ACTIVE");
        }
        return owner;
    }

    /**
     * Generates a unique account number for a joint account.
     *
     * <p>This method generates a unique account number by prefixing the generated number with "JA".</p>
     *
     * @return a unique joint account number
     */
    private String generateUniqueJointAccountNumber() {
        return numberGenerator.generateUniqueAccountNumber("JA");
    }

    /**
     * Validates the provided joint account for creation, ensuring it meets all necessary criteria.
     *
     * @param jointAccount the joint account to validate
     * @throws BusinessException if any validation criteria are not met
     */
    private void validateForCreate(JointAccount jointAccount) {
        if (jointAccount == null) {
            throw new BusinessException("Joint account cannot be null");
        }
        if (jointAccount.getBalance() == null || jointAccount.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("Joint account must be created with a balance of zero");
        }
        if (jointAccount.getOpeningDate() == null) {
            throw new BusinessException("Joint account must have an opening date");
        }
        if (!jointAccount.getOpeningDate().isEqual(LocalDate.now())) {
            throw new BusinessException("Joint account opening date must be today");
        }
        if (jointAccount.getOwners() == null || jointAccount.getOwners().isEmpty()) {
            throw new BusinessException("Joint account must have at least one owner");
        }
        if (jointAccount.getStatus() == null) {
            throw new BusinessException("Joint account must have a status");
        }
        if (jointAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Joint account must be ACTIVE");
        }
        for (Client owner : jointAccount.getOwners()) {
            if (owner.getStatus() != ClientStatus.ACTIVE) {
                throw new BusinessException("All owners must be ACTIVE");
            }
        }
        if (jointAccount.getOwners().size() != 2) {
            throw new BusinessException("Joint account must have two owners");
        }
        Set<Client> activeOwners = new HashSet<>();

        for (Client owner : jointAccount.getOwners()) {
            if (owner == null) {
                throw new BusinessException("Joint account owner cannot be null");
            }
            activeOwners.add(getActiveOwnersById(owner.getId()));
        }

        jointAccount.getOwners().clear();
        jointAccount.getOwners().addAll(activeOwners);
    }


}
