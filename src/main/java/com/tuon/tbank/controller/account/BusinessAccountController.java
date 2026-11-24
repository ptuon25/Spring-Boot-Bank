package com.tuon.tbank.controller.account;

import com.tuon.tbank.dto.account.*;
import com.tuon.tbank.entity.account.BusinessAccount;
import com.tuon.tbank.enums.company.CompanyMemberRole;
import com.tuon.tbank.enums.company.MembershipStatus;
import com.tuon.tbank.service.account.AccountService;
import com.tuon.tbank.service.account.BusinessAccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controller for managing business accounts.
 * <p>This controller provides endpoints for performing CRUD operations on business accounts.</p>
 */
@RestController
@RequestMapping("/accounts/business-accounts")
public class BusinessAccountController {

    private final BusinessAccountService businessAccountService;
    private final AccountService accountService;

    /**
     * Constructs a new BusinessAccountController with the specified BusinessAccountService.
     *
     * @param businessAccountService the service for managing business accounts
     * @param accountService         the service for managing accounts
     */
    public BusinessAccountController(BusinessAccountService businessAccountService, AccountService accountService) {
        this.businessAccountService = businessAccountService;
        this.accountService = accountService;
    }

    /**
     * Retrieves all business accounts.
     *
     * @return a ResponseEntity containing a list of AccountProfileResponse objects or no content if none found
     */
    @GetMapping
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<List<AccountProfileResponse>> getAllBusinessAccounts() {
        List<AccountProfileResponse> response = toProfiles(businessAccountService.getAllBusinessAccounts());
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    /**
     * Retrieves a business account by its ID.
     *
     * @param accountId the ID of the business account to retrieve
     * @return a ResponseEntity containing the AccountProfileResponse or not found if the account does not exist
     */
    @GetMapping("/{accountId}")
    @PreAuthorize("@accountAuthorization.canReadBusinessAccount(#accountId, authentication)")
    public ResponseEntity<AccountProfileResponse> getBusinessAccountById(@PathVariable Long accountId) {
        BusinessAccount businessAccount = businessAccountService.getBusinessAccountById(accountId);
        return ResponseEntity.ok(mapToAccountProfileResponse(businessAccount));
    }

    /**
     * Retrieves all business accounts for a given company.
     *
     * @param companyId the ID of the company
     * @return a ResponseEntity containing a list of AccountProfileResponse objects or no content if none found
     */
    @GetMapping("/search/company/{companyId}")
    @PreAuthorize("@companyAuthorization.canReadCompany(#companyId, authentication)")
    public ResponseEntity<List<AccountProfileResponse>> getBusinessAccountByCompanyId(@PathVariable Long companyId) {
        List<BusinessAccount> businessAccounts = businessAccountService.getBusinessAccountsByCompanyId(companyId);
        List<AccountProfileResponse> response = toProfiles(businessAccounts);
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    /**
     * Retrieves a business account by owner ID and account ID.
     *
     * @param ownerId   the ID of the owner
     * @param accountId the ID of the business account
     * @return a ResponseEntity containing the AccountProfileResponse or not found if the account does not exist
     */
    @GetMapping("/search/owner/{ownerId}/account/{accountId}")
    @PreAuthorize("@accountAuthorization.canReadBusinessAccount(#accountId, authentication)")
    public ResponseEntity<AccountProfileResponse> getBusinessAccountByOwnerIdAndAccountId(@PathVariable Long ownerId, @PathVariable Long accountId) {
        BusinessAccount businessAccount = businessAccountService.getBusinessAccountByOwnerIdAndAccountId(ownerId, accountId);
        return ResponseEntity.ok(mapToAccountProfileResponse(businessAccount));
    }

    /**
     * Retrieves all business accounts accessible by a specific client, filtered by membership status and roles.
     *
     * @param clientId the ID of the client
     * @param status   the membership status to filter by
     * @param roles    the list of company member roles to filter by
     * @return a ResponseEntity containing a list of AccountProfileResponse objects or no content if none found
     */
    @GetMapping("/search/client/{clientId}")
    @PreAuthorize("@accountAuthorization.canListBusinessAccountsForClient(#clientId, authentication)")
    public ResponseEntity<List<AccountProfileResponse>> getBusinessAccountByAccessibleByClient(@PathVariable Long clientId,
                                                                                               @RequestParam MembershipStatus status,
                                                                                               @RequestParam List<CompanyMemberRole> roles) {
        List<BusinessAccount> businessAccounts = businessAccountService.getBusinessAccountByAccessibleByClient(clientId, status, roles);
        List<AccountProfileResponse> response = toProfiles(businessAccounts);
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    /**
     * Retrieves a business account by its account number.
     *
     * @param number the account number
     * @return a ResponseEntity containing the AccountProfileResponse or not found if the account does not exist
     */
    @GetMapping("/search/number")
    @PreAuthorize("@accountAuthorization.canReadBusinessAccountByNumber(#number, authentication)")
    public ResponseEntity<AccountProfileResponse> getBusinessAccountByNumber(@RequestParam String number) {
        BusinessAccount businessAccount = businessAccountService.getBusinessAccountByAccountNumber(number);
        return ResponseEntity.ok(mapToAccountProfileResponse(businessAccount));
    }

    /**
     * Retrieves the balance of a business account by its ID.
     *
     * @param accountId the ID of the business account
     * @return a ResponseEntity containing the AccountBalanceResponse or not found if the account does not exist
     */
    @GetMapping("/{accountId}/balance")
    @PreAuthorize("@accountAuthorization.canReadBusinessAccount(#accountId, authentication)")
    public ResponseEntity<AccountBalanceResponse> getBusinessAccountBalance(@PathVariable Long accountId) {
        AccountBalanceResponse balance = businessAccountService.getBusinessAccountBalance(accountId);
        return ResponseEntity.ok(balance);
    }

    /**
     * Retrieves the balance of a business account by its account number.
     *
     * @param request the account creation request
     * @return a ResponseEntity containing the AccountBalanceResponse or not found if the account does not exist
     */
    @PostMapping
    @PreAuthorize("@accountAuthorization.canOpenBusinessAccountFor(#request.companyId(), authentication)")
    public ResponseEntity<BusinessAccountCreateResponse> insertBusinessAccount(@Valid @RequestBody BusinessAccountCreateRequest request) {
        BusinessAccount businessAccount = businessAccountService.registerBusinessAccount(
                request.companyId());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(businessAccount.getId()).toUri();
        return ResponseEntity.created(uri).body(new BusinessAccountCreateResponse(
                businessAccount.getId(),
                businessAccount.getAccountNumber(),
                businessAccount.getOpeningDate(),
                businessAccount.getOwner()
        ));
    }

    /**
     * Blocks a business account by its ID and account number.
     *
     * @param accountId the ID of the business account to block
     * @param request   the account status update request containing the account number
     * @return a ResponseEntity containing the AccountProfileResponse of the blocked account
     */
    @PatchMapping("/{accountId}/block")
    @PreAuthorize("@accountAuthorization.canManageBusinessAccountByNumber(#accountId, authentication)")
    public ResponseEntity<AccountProfileResponse> blockBusinessAccount(@PathVariable Long accountId,
                                                                       @Valid @RequestBody AccountStatusUpdateRequest request) {
        return ResponseEntity.ok(mapToAccountProfileResponse(businessAccountService.blockBusinessAccount(accountId, request.accountNumber())));
    }

    /**
     * Suspends a business account by its ID and account number.
     *
     * @param accountId the ID of the business account to suspend
     * @param request   the account status update request containing the account number
     * @return a ResponseEntity containing the AccountProfileResponse of the suspended account
     */
    @PatchMapping("/{accountId}/suspend")
    @PreAuthorize("@accountAuthorization.canManageBusinessAccountByNumber(#accountId, authentication)")
    public ResponseEntity<AccountProfileResponse> suspendBusinessAccount(@PathVariable Long accountId,
                                                                         @Valid @RequestBody AccountStatusUpdateRequest request) {
        return ResponseEntity.ok(mapToAccountProfileResponse(businessAccountService.suspendBusinessAccount(accountId, request.accountNumber())));
    }

    /**
     * Reactivates a business account by its ID and account number.
     *
     * @param accountId the ID of the business account to reactivate
     * @param request   the account status update request containing the account number
     * @return a ResponseEntity containing the AccountProfileResponse of the reactivated account
     */
    @PatchMapping("/{accountId}/reactivate")
    @PreAuthorize("@accountAuthorization.canManageBusinessAccountByNumber(#accountId, authentication)")
    public ResponseEntity<AccountProfileResponse> reactivateBusinessAccount(@PathVariable Long accountId,
                                                                           @Valid @RequestBody AccountStatusUpdateRequest request) {
        return ResponseEntity.ok(mapToAccountProfileResponse(businessAccountService.reactivateBusinessAccount(accountId, request.accountNumber())));
    }

    /**
     * Updates the owner of a business account.
     *
     * @param accountId  the ID of the business account to update
     * @param newOwnerId the ID of the new owner
     * @return a ResponseEntity containing the AccountProfileResponse of the updated account
     */
    @PutMapping("/{accountId}/owner/{newOwnerId}")
    @PreAuthorize("@accountAuthorization.canAdministerBusinessAccount(#accountId, authentication)")
    public ResponseEntity<AccountProfileResponse> updateBusinessAccountOwner(@PathVariable Long accountId,
                                                                            @PathVariable Long newOwnerId) {
        return ResponseEntity.ok(mapToAccountProfileResponse(businessAccountService.updateBusinessAccountOwner(accountId, newOwnerId)));
    }

    /**
     * Deletes a business account.
     *
     * @param accountId the ID of the business account to delete
     * @return a ResponseEntity with no content
     */
    @DeleteMapping(value = "/{accountId}")
    @PreAuthorize("@accountAuthorization.canManageBusinessAccount(#accountId, authentication)")
    public ResponseEntity<Void> deleteBusinessAccount(@PathVariable Long accountId) {
        businessAccountService.deleteBusinessAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Restores a deleted business account.
     *
     * @param accountId the ID of the business account to restore
     * @return a ResponseEntity containing the AccountProfileResponse of the restored account
     */
    @PatchMapping(value = "/{accountId}/restore")
    @PreAuthorize("@accountAuthorization.canManageBusinessAccount(#accountId, authentication)")
    public ResponseEntity<AccountProfileResponse> restoreBusinessAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(mapToAccountProfileResponse(businessAccountService.restoreBusinessAccount(accountId)));
    }

    // AUXILIARY

    private AccountProfileResponse mapToAccountProfileResponse(BusinessAccount account) {
        return new AccountProfileResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getOpeningDate(),
                account.getStatus()
        );
    }

    private List<AccountProfileResponse> toProfiles(List<BusinessAccount> accounts) {
        return accounts.stream()
                .map(this::mapToAccountProfileResponse)
                .toList();
    }
}
