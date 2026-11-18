package com.tuon.tbank.service.account;

import com.tuon.tbank.dto.account.AccountBalanceResponse;
import com.tuon.tbank.entity.account.BusinessAccount;
import com.tuon.tbank.entity.company.Company;
import com.tuon.tbank.entity.company.CompanyMembership;
import com.tuon.tbank.enums.account.AccountStatus;
import com.tuon.tbank.enums.company.CompanyMemberRole;
import com.tuon.tbank.enums.company.MembershipStatus;
import com.tuon.tbank.exception.serviceException.BusinessException;
import com.tuon.tbank.exception.serviceException.DatabaseException;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.account.BusinessAccountRepository;
import com.tuon.tbank.repository.company.CompanyMembershipRepository;
import com.tuon.tbank.repository.company.CompanyRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing business accounts.
 * <p>This service provides methods to perform CRUD operations and business logic related to business accounts.</p>
 */
@Service
public class BusinessAccountService {

    private final BusinessAccountRepository businessAccountRepository;
    private final AccountNumberGenerator numberGenerator;
    private final CompanyRepository companyRepository;
    private final CompanyMembershipRepository companyMembershipRepository;

    /**
     * Constructs a new BusinessAccountService with the specified repositories and number generator.
     *
     * @param businessAccountRepository the repository for business accounts
     * @param numberGenerator           the generator for unique account numbers
     * @param companyRepository         the repository for companies
     * @param companyMembershipRepository the repository for company memberships
     */
    public BusinessAccountService(BusinessAccountRepository businessAccountRepository,
                                  AccountNumberGenerator numberGenerator,
                                  CompanyRepository companyRepository,
                                  CompanyMembershipRepository companyMembershipRepository) {
        this.businessAccountRepository = businessAccountRepository;
        this.numberGenerator = numberGenerator;
        this.companyRepository = companyRepository;
        this.companyMembershipRepository = companyMembershipRepository;
    }

    /**
     * Retrieves all business accounts that are not marked as deleted.
     *
     * @return a list of all non-deleted business accounts
     */
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public List<BusinessAccount> getAllBusinessAccounts() {
        return businessAccountRepository.findByDeletedFalse();
    }

    /**
     * Retrieves a business account by its ID, ensuring it is not marked as deleted.
     *
     * @param accountId the ID of the business account to retrieve
     * @return the found business account
     * @throws BusinessException         if the account ID is null or invalid
     * @throws ResourceNotFoundException if no business account is found with the given ID
     */
    @PreAuthorize("@accountAuthorization.canReadBusinessAccount(#accountId, authentication)")
    public BusinessAccount getBusinessAccountById(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }

        return businessAccountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business account not found with id: " + accountId));
    }

    /**
     * Retrieves all business accounts associated with a specific company, ensuring the company is not marked as deleted.
     *
     * @param companyId the ID of the company
     * @return a list of business accounts associated with the specified company
     * @throws BusinessException if the company ID is null or invalid
     */
    @PreAuthorize("@companyAuthorization.canReadCompany(#companyId, authentication)")
    public List<BusinessAccount> getBusinessAccountsByCompanyId(Long companyId) {
        if (companyId == null || companyId <= 0) {
            throw new BusinessException("Company ID cannot be null or zero");
        }

        return businessAccountRepository.findByOwnerIdAndOwnerDeletedFalseAndDeletedFalse(companyId);
    }

    /**
     * Retrieves a business account by its owner ID and account ID, ensuring both are not marked as deleted.
     *
     * @param ownerId   the ID of the company (owner)
     * @param accountId the ID of the business account
     * @return the found business account
     * @throws BusinessException         if either ID is null or invalid
     * @throws ResourceNotFoundException if no business account is found with the given owner and account IDs
     */
    @PreAuthorize("@accountAuthorization.canReadBusinessAccount(#accountId, authentication)")
    public BusinessAccount getBusinessAccountByOwnerIdAndAccountId(Long ownerId, Long accountId) {
        if (ownerId == null || ownerId <= 0) {
            throw new BusinessException("Owner ID cannot be null or zero");
        }
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }

        return businessAccountRepository.findByOwnerIdAndIdAndDeletedFalseAndOwnerDeletedFalse(ownerId, accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business account not found for the given owner and account ID"));
    }

    /**
     * Retrieves a list of business accounts that are accessible by a specific client, based on their membership status and roles.
     *
     * @param clientId the ID of the client
     * @param status   the membership status to check
     * @param roles    the list of roles to check
     * @return a list of business accounts accessible by the specified client
     * @throws BusinessException if any of the parameters are null or invalid
     */
    @PreAuthorize("@accountAuthorization.canListBusinessAccountsForClient(#clientId, authentication)")
    public List<BusinessAccount> getBusinessAccountByAccessibleByClient(Long clientId, MembershipStatus status, List<CompanyMemberRole> roles) {
        if (clientId == null || clientId <= 0) {
            throw new BusinessException("Client ID cannot be null or zero");
        }
        if (status == null) {
            throw new BusinessException("Membership status cannot be null");
        }
        if (roles == null || roles.isEmpty()) {
            throw new BusinessException("Roles cannot be null or empty");
        }
        return businessAccountRepository.findAccessibleByClient(clientId, status, List.copyOf(roles));
    }

    /**
     * Builds a valid business account number by ensuring it has the correct prefix and format.
     *
     * @param number the raw account number input
     * @return a formatted business account number with the "BA-" prefix
     * @throws BusinessException if the input number is null or does not contain exactly 8 digits
     */
    @PreAuthorize("@accountAuthorization.canReadBusinessAccountByNumber(#number, authentication)")
    public BusinessAccount getBusinessAccountByAccountNumber(String number) {
        String accountNumber = buildBusinessAccountNumber(number);

        return businessAccountRepository
                .findByAccountNumberAndDeletedFalse(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business account not found by account number: " + accountNumber));
    }

    /**
     * Retrieves the balance of a business account by its ID.
     *
     * @param accountId the ID of the business account
     * @return an AccountBalanceResponse containing the account's balance
     * @throws BusinessException         if the account ID is null or invalid
     * @throws ResourceNotFoundException if no business account is found with the given ID
     */
    @PreAuthorize("@accountAuthorization.canReadBusinessAccount(#accountId, authentication)")
    public AccountBalanceResponse getBusinessAccountBalance(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }

        BusinessAccount businessAccount = businessAccountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business account not found with id: " + accountId));

        return new AccountBalanceResponse(
                businessAccount.getId(),
                businessAccount.getAccountNumber(),
                businessAccount.getBalance());
    }

    /**
     * Registers a new business account with the specified details.
     * <p>This method will create a new business account with the provided details and save it to the database.</p>
     *
     * @param balance     the initial balance of the account
     * @param openingDate the date when the account was opened
     * @param owner       the company that owns this business account
     * @return the newly created business account
     * @throws BusinessException if any of the parameters are null or invalid
     */
    @PreAuthorize("@accountAuthorization.canOpenBusinessAccountFor(#owner?.id, authentication)")
    public BusinessAccount registerBusinessAccount(BigDecimal balance, LocalDate openingDate, Company owner) {
        if (balance == null) {
            throw new BusinessException("Balance cannot be null");
        }
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Balance cannot be negative");
        }
        if (openingDate == null) {
            throw new BusinessException("Opening date cannot be null");
        }
        if (owner == null) {
            throw new BusinessException("Owner cannot be null");
        }

        owner = getActiveOwnerById(owner.getId());
        BusinessAccount businessAccount = new BusinessAccount(
                null, null, balance, openingDate, AccountStatus.ACTIVE, owner);

        return createBusinessAccount(businessAccount);
    }

    /**
     * Creates a new business account in the system.
     *
     * @param businessAccount the business account to be created
     * @return the created business account
     * @throws BusinessException if the business account is null or invalid
     * @throws DatabaseException if there is a database constraint violation during creation
     */
    @PreAuthorize("@accountAuthorization.canOpenBusinessAccountFor(#businessAccount?.owner?.id, authentication)")
    public BusinessAccount createBusinessAccount(BusinessAccount businessAccount) {
        validateForCreate(businessAccount);
        businessAccount.setAccountNumber(generateUniqueBusinessAccountNumber());
        if (businessAccount.getAccountNumber() == null || businessAccount.getAccountNumber().isEmpty()) {
            throw new BusinessException("Account number cannot be null or empty");
        }
        try {
            return businessAccountRepository.saveAndFlush(businessAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not create business account due to a database constraint.");
        }
    }

    /**
     * Blocks a business account by its id and account number.
     *
     * @param accountNumber the account number of the business account to block
     * @return the blocked business account
     * @throws BusinessException         if the account is already blocked or suspended, or if the account number is invalid
     * @throws ResourceNotFoundException if no business account is found with the given account number
     * @throws DatabaseException         if there is a database constraint violation during blocking
     */
    @PreAuthorize("@accountAuthorization.canManageBusinessAccountByNumber(#accountNumber, authentication)")
    public BusinessAccount blockBusinessAccount(Long id, String accountNumber) {
        if(id == null || id <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        String normalizedAccountNumber = buildBusinessAccountNumber(accountNumber);
        BusinessAccount businessAccount = businessAccountRepository.findByAccountNumberAndDeletedFalse(normalizedAccountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Business account not found with account number: " + normalizedAccountNumber));
        BusinessAccount accountById = businessAccountRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business account not found with id: " + id));
        if (!businessAccount.getId().equals(accountById.getId())) {
            throw new BusinessException("Account number does not match the provided account ID.");
        }
        if (businessAccount.getStatus() == AccountStatus.BLOCKED) {
            throw new BusinessException("Business account is already blocked.");
        }
        if (businessAccount.getStatus() == AccountStatus.SUSPENDED) {
            throw new BusinessException("Business account is suspended and cannot be blocked.");
        }
        businessAccount.setStatus(AccountStatus.BLOCKED);
        try {
            return businessAccountRepository.saveAndFlush(businessAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not block business account due to a database constraint.");
        }
    }

    /**
     * Suspends a business account by its id and account number.
     *
     * @param accountNumber the account number of the business account to suspend
     * @return the suspended business account
     * @throws BusinessException         if the account is already suspended or blocked, or if the account number is invalid
     * @throws ResourceNotFoundException if no business account is found with the given account number
     * @throws DatabaseException         if there is a database constraint violation during suspension
     */
    @PreAuthorize("@accountAuthorization.canManageBusinessAccountByNumber(#accountNumber, authentication)")
    public BusinessAccount suspendBusinessAccount(Long id, String accountNumber) {
        String normalizedAccountNumber = buildBusinessAccountNumber(accountNumber);
        BusinessAccount businessAccount = businessAccountRepository.findByAccountNumberAndDeletedFalse(normalizedAccountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Business account not found with account number: " + normalizedAccountNumber));
        BusinessAccount accountById = businessAccountRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business account not found with id: " + id));
        if (!businessAccount.getId().equals(accountById.getId())) {
            throw new BusinessException("Account number does not match the provided account ID.");
        }
        if (businessAccount.getStatus() == AccountStatus.SUSPENDED) {
            throw new BusinessException("Business account is already suspended.");
        }
        if (businessAccount.getStatus() == AccountStatus.BLOCKED) {
            throw new BusinessException("Business account is blocked and cannot be suspended.");
        }
        businessAccount.setStatus(AccountStatus.SUSPENDED);
        try {
            return businessAccountRepository.saveAndFlush(businessAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not suspend business account due to a database constraint.");
        }
    }

    /**
     * Reactivates a business account that is currently blocked or suspended.
     *
     * @param accountNumber the account number of the business account to reactivate
     * @return the reactivated business account
     * @throws BusinessException         if the account is already active or if it cannot be reactivated
     * @throws ResourceNotFoundException if no business account is found with the given account number
     * @throws DatabaseException         if there is a database constraint violation during reactivation
     */
    @PreAuthorize("@accountAuthorization.canManageBusinessAccountByNumber(#accountNumber, authentication)")
    public BusinessAccount reactivateBusinessAccount(Long id, String accountNumber) {
        if(id == null || id <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        String normalizedAccountNumber = buildBusinessAccountNumber(accountNumber);
        BusinessAccount businessAccount = businessAccountRepository.findByAccountNumberAndDeletedFalse(normalizedAccountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Business account not found with account number: " + normalizedAccountNumber));
        BusinessAccount accountById = businessAccountRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business account not found with id: " + id));
        if (!businessAccount.getId().equals(accountById.getId())) {
            throw new BusinessException("Account number does not match the provided account ID.");
        }
        if (businessAccount.getStatus() == AccountStatus.ACTIVE) {
            throw new BusinessException("Business account is already active.");
        }
        businessAccount.setStatus(AccountStatus.ACTIVE);
        try {
            return businessAccountRepository.saveAndFlush(businessAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not reactivate business account due to a database constraint.");
        }
    }

    /**
     * Updates the owner of a business account.
     *
     * @param accountId  the ID of the business account to update
     * @param newOwnerId the ID of the new owner company
     * @return the updated business account
     * @throws BusinessException         if any of the parameters are null or invalid, or if the new owner is the same as the current owner
     * @throws ResourceNotFoundException if no business account is found with the given ID, or if no active company is found with the new owner ID
     * @throws DatabaseException         if there is a database constraint violation during the update
     */
    @PreAuthorize("@accountAuthorization.canAdministerBusinessAccount(#accountId, authentication)")
    public BusinessAccount updateBusinessAccountOwner(Long accountId, Long newOwnerId) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }
        if (newOwnerId == null || newOwnerId <= 0) {
            throw new BusinessException("New owner ID cannot be null or zero");
        }
        Optional<CompanyMembership> member = companyMembershipRepository.findById(newOwnerId);
        if( member.isEmpty() || member.get().getStatus() != MembershipStatus.ACTIVE) {
            throw new BusinessException("New owner must be an active member of the company");
        }
        BusinessAccount businessAccount = businessAccountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Business account not found with id: " + accountId));

        Company newOwner = getActiveOwnerById(newOwnerId);
        if (businessAccount.getOwner() != null
                && businessAccount.getOwner().getId() != null
                && businessAccount.getOwner().getId().equals(newOwner.getId())) {
            throw new BusinessException("Business account already belongs to this company.");
        }
        businessAccount.setOwner(newOwner);
        try {
            return businessAccountRepository.saveAndFlush(businessAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not update business account owner due to a database constraint.");
        }
    }

    /**
     * Deletes a business account by marking it as deleted in the database.
     *
     * @param accountId the ID of the business account to delete
     * @throws BusinessException         if the account ID is null or invalid
     * @throws ResourceNotFoundException if no business account is found with the given ID
     * @throws DatabaseException         if there is a database constraint violation during deletion
     */
    @PreAuthorize("@accountAuthorization.canManageBusinessAccount(#accountId, authentication)")
    public void deleteBusinessAccount(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }

        BusinessAccount businessAccount = businessAccountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Business account not found with id: " + accountId));

        businessAccount.setDeleted(true);
        try {
            businessAccountRepository.saveAndFlush(businessAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not delete business account due to a database constraint.");
        }
    }

    @PreAuthorize("@accountAuthorization.canManageBusinessAccount(#accountId, authentication)")
    public BusinessAccount restoreBusinessAccount(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new BusinessException("Account ID cannot be null or zero");
        }

        BusinessAccount businessAccount = businessAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Business account not found with id: " + accountId));

        businessAccount.setDeleted(false);
        try {
            return businessAccountRepository.saveAndFlush(businessAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not restore business account due to a database constraint.");
        }
    }

    // AUXILIARY

    /**
     * Builds a valid business account number by ensuring it has the correct prefix and format.
     *
     * @param number the raw account number input
     * @return a formatted business account number with the "BA-" prefix
     * @throws BusinessException if the input number is null or does not contain exactly 8 digits
     */
    private String buildBusinessAccountNumber(String number) {
        if (number == null) {
            throw new BusinessException("Account number must contain exactly 8 digits");
        }

        String numericNumber = number.startsWith("BA-")
                ? number.substring(3)
                : number;

        if (!numericNumber.matches("\\d{8}")) {
            throw new BusinessException("Account number must contain exactly 8 digits");
        }

        return "BA-" + numericNumber;
    }

    /**
     * Retrieves an active company by its ID, ensuring it is not marked as deleted.
     *
     * @param id the ID of the company to retrieve
     * @return the found company
     * @throws BusinessException         if the ID is null or invalid
     * @throws ResourceNotFoundException if no active company is found with the given ID
     */
    private Company getActiveOwnerById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("Company ID cannot be null or zero");
        }
        Optional<Company> company = companyRepository.findByIdAndDeletedFalse(id);
        return company.orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    }

    /**
     * Generates a unique account number for a business account.
     *
     * <p>This method generates a unique account number by prefixing the generated number with "BA".</p>
     *
     * @return a unique business account number
     */
    private String generateUniqueBusinessAccountNumber() {
        return numberGenerator.generateUniqueAccountNumber("BA");
    }

    /**
     * Validates the provided business account for creation.
     *
     * @param businessAccount the business account to validate
     * @throws BusinessException if any validation fails
     */
    private void validateForCreate(BusinessAccount businessAccount) {
        if (businessAccount == null) {
            throw new BusinessException("Account cannot be null");
        }
        if (businessAccount.getBalance() == null || businessAccount.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Business Account balance cannot be null or negative");
        }
        if (businessAccount.getOpeningDate() == null) {
            throw new BusinessException("Business Account opening date cannot be null");
        }
        if (businessAccount.getStatus() == null) {
            throw new BusinessException("Business Account status cannot be null");
        }
        if (businessAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Business Account must be created with ACTIVE status");
        }
        if (businessAccount.getOwner() == null) {
            throw new BusinessException("Business Account must have an owner company");
        }
        businessAccount.setOwner(getActiveOwnerById(businessAccount.getOwner().getId()));
    }

}
