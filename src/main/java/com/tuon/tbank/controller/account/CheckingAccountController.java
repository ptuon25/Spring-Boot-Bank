package com.tuon.tbank.controller.account;

import com.tuon.tbank.dto.account.*;
import com.tuon.tbank.entity.account.CheckingAccount;
import com.tuon.tbank.service.account.AccountService;
import com.tuon.tbank.service.account.CheckingAccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/checking-accounts")
public class CheckingAccountController {

    private final CheckingAccountService checkingAccountService;
    private final AccountService accountService;

    public CheckingAccountController(CheckingAccountService checkingAccountService, AccountService accountService) {
        this.checkingAccountService = checkingAccountService;
        this.accountService = accountService;
    }

    /**
     * Retrieves all checking accounts in the system.
     * This endpoint is secured and requires the user to have the appropriate authorization to search for accounts.
     *
     * @return a ResponseEntity containing a list of AccountProfileResponse objects representing the checking accounts,
     * or a no content response if no accounts are found
     */
    @GetMapping
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<List<AccountProfileResponse>> getAllCheckingAccounts() {
        List<AccountProfileResponse> response = toProfiles(checkingAccountService.getAllCheckingAccounts());
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    /**
     * Retrieves a checking account by its owner's ID.
     * This endpoint is secured and requires the user to have the appropriate authorization to search for accounts.
     *
     * @param ownerId the ID of the account owner
     * @return a ResponseEntity containing an AccountProfileResponse object representing the checking account,
     * or a not found response if no account is found
     */
    @GetMapping("/search/owner/ownerId")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> getCheckingAccountByOwnerId(@PathVariable Long ownerId) {
        CheckingAccount account = checkingAccountService.getCheckingAccountByOwnerId(ownerId);
        return ResponseEntity.ok(mapToAccountProfileResponse(account));
    }

    /**
     * Retrieves a checking account by its ID and owner's ID.
     * This endpoint is secured and requires the user to have the appropriate authorization to search for accounts.
     *
     * @param accountId the ID of the checking account
     * @param ownerId   the ID of the account owner
     * @return a ResponseEntity containing an AccountProfileResponse object representing the checking account,
     * or a not found response if no account is found
     */
    @GetMapping("/search/owner/{ownerId}/account/{accountId}")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> getCheckingAccountByIdAndOwnerId(@PathVariable Long accountId, @PathVariable Long ownerId) {
        CheckingAccount account = checkingAccountService.getCheckingAccountByIdAndOwnerId(accountId, ownerId);
        return ResponseEntity.ok(mapToAccountProfileResponse(account));
    }

    /**
     * Retrieves a checking account by its account number.
     * This endpoint is secured and requires the user to have the appropriate authorization to search for accounts.
     *
     * @param accountNumber the account number of the checking account
     * @return a ResponseEntity containing an AccountProfileResponse object representing the checking account,
     * or a not found response if no account is found
     */
    @GetMapping("/search/number")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> getCheckingAccountByAccountNumber(@RequestParam String accountNumber) {
        CheckingAccount account = checkingAccountService.getCheckingAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(mapToAccountProfileResponse(account));
    }

    /**
     * Retrieves all deleted checking accounts in the system.
     * This endpoint is secured and requires the user to have the appropriate authorization to search for accounts.
     *
     * @return a ResponseEntity containing a list of AccountProfileResponse objects representing the deleted checking accounts,
     * or a no content response if no deleted accounts are found
     */
    @GetMapping("/search/deleted")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<List<AccountProfileResponse>> getAllDeletedCheckingAccounts() {
        List<AccountProfileResponse> response = toProfiles(checkingAccountService.getAllDeletedCheckingAccounts());
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    /**
     * Retrieves a deleted checking account by its ID.
     * This endpoint is secured and requires the user to have the appropriate authorization to search for accounts.
     *
     * @param accountId the ID of the deleted checking account
     * @return a ResponseEntity containing an AccountProfileResponse object representing the deleted checking account,
     * or a not found response if no deleted account is found
     */
    @GetMapping("/search/deleted/{accountId}")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> getDeletedCheckingAccountById(@PathVariable Long accountId) {
        CheckingAccount account = checkingAccountService.getDeletedCheckingAccountById(accountId);
        return ResponseEntity.ok(mapToAccountProfileResponse(account));
    }

    /**
     * Retrieves the balance of a checking account by its ID.
     * This endpoint is secured and requires the user to have the appropriate authorization to access the checking account.
     *
     * @param accountId the ID of the checking account
     * @return a ResponseEntity containing an AccountBalanceResponse object representing the balance of the checking account,
     * or a not found response if no account is found
     */
    @GetMapping("/{accountId}/balance")
    @PreAuthorize("@accountAuthorization.canAccessCheckingAccount(#accountId, authentication)")
    public ResponseEntity<AccountBalanceResponse> getCheckingAccountBalance(@PathVariable Long accountId) {
        AccountBalanceResponse balance = checkingAccountService.getCheckingAccountBalance(accountId);
        return ResponseEntity.ok(balance);
    }

    /**
     * Creates a new checking account in the system.
     * This endpoint is secured and requires the user to have the appropriate authorization to open an account for the specified owner.
     *
     * @param request the CheckingAccountCreateRequest object containing the details of the new checking account
     * @return a ResponseEntity containing a CheckingAccountCreateResponse object representing the newly created checking account,
     * or a bad request response if the request is invalid
     */
    @PostMapping
    @PreAuthorize("@accountAuthorization.canOpenAccountFor(#checkingAccount?.owner?.id, authentication)")
    public ResponseEntity<CheckingAccountCreateResponse> insertCheckingAccount(@Valid @RequestBody CheckingAccountCreateRequest request) {
        CheckingAccount checkingAccount = checkingAccountService.registerCheckingAccount(
                request.balance(),
                request.openingDate(),
                request.owner()
        );
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(checkingAccount.getId()).toUri();
        return ResponseEntity.created(uri).body((new CheckingAccountCreateResponse(
                checkingAccount.getId(),
                checkingAccount.getAccountNumber(),
                checkingAccount.getOpeningDate(),
                checkingAccount.getOwner()
        )));
    }

    /**
     * Blocks a checking account by its ID.
     * This endpoint is secured and requires the user to have the appropriate authorization to manage the specified account.
     *
     * @param id      the ID of the checking account to be blocked
     * @param request the AccountStatusUpdateRequest object containing the account number for verification
     * @return a ResponseEntity containing an AccountProfileResponse object representing the blocked checking account,
     * or a bad request response if the request is invalid
     */
    @PatchMapping("/{id}/block")
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> blockCheckingAccount(@PathVariable Long id,
                                                                       @Valid @RequestBody AccountStatusUpdateRequest request) {
        return ResponseEntity.ok(mapToAccountProfileResponse(checkingAccountService.blockCheckingAccount(id, request.accountNumber())));
    }

    /**
     * Suspends a checking account by its ID.
     * This endpoint is secured and requires the user to have the appropriate authorization to manage the specified account.
     *
     * @param id      the ID of the checking account to be suspended
     * @param request the AccountStatusUpdateRequest object containing the account number for verification
     * @return a ResponseEntity containing an AccountProfileResponse object representing the suspended checking account,
     * or a bad request response if the request is invalid
     */
    @PatchMapping("/{id}/suspend")
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> suspendCheckingAccount(@PathVariable Long id,
                                                                         @Valid @RequestBody AccountStatusUpdateRequest request) {
        return ResponseEntity.ok(mapToAccountProfileResponse(checkingAccountService.suspendCheckingAccount(id, request.accountNumber())));
    }

    /**
     * Reactivates a checking account by its ID.
     * This endpoint is secured and requires the user to have the appropriate authorization to manage the specified account.
     *
     * @param id      the ID of the checking account to be reactivated
     * @param request the AccountStatusUpdateRequest object containing the account number for verification
     * @return a ResponseEntity containing an AccountProfileResponse object representing the reactivated checking account,
     * or a bad request response if the request is invalid
     */
    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> reactivateCheckingAccount(@PathVariable Long id,
                                                                            @Valid @RequestBody AccountStatusUpdateRequest request) {
        return ResponseEntity.ok(mapToAccountProfileResponse(checkingAccountService.reactivateCheckingAccount(id, request.accountNumber())));
    }

    /**
     * Updates the owner of a checking account.
     * This endpoint is secured and requires the user to have the appropriate authorization to administer the specified account.
     *
     * @param accountId  the ID of the checking account whose owner is to be updated
     * @param newOwnerId the ID of the new owner for the checking account
     * @return a ResponseEntity containing an AccountProfileResponse object representing the updated checking account,
     * or a bad request response if the request is invalid
     */
    @PutMapping("{accountId}/owner/{newOwnerId}")
    @PreAuthorize("@accountAuthorization.canAdministerAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> updateCheckingAccountOwner(@PathVariable Long accountId, @PathVariable Long newOwnerId) {
        return ResponseEntity.ok(mapToAccountProfileResponse(checkingAccountService.updateCheckingAccountOwner(accountId, newOwnerId)));
    }

    /**
     * Deletes a checking account by its ID.
     * This endpoint is secured and requires the user to have the appropriate authorization to manage the specified account.
     *
     * @param id the ID of the checking account to be deleted
     * @return a ResponseEntity with no content indicating successful deletion,
     * or a not found response if no account is found
     */
    @DeleteMapping(value = "/{id}")
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public ResponseEntity<Void> deleteCheckingAccount(@PathVariable Long id) {
        checkingAccountService.deleteCheckingAccount(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Restores a deleted checking account by its ID.
     * This endpoint is secured and requires the user to have the appropriate authorization to manage the specified account.
     *
     * @param accountId the ID of the checking account to be restored
     * @return a ResponseEntity containing an AccountProfileResponse object representing the restored checking account,
     * or a not found response if no deleted account is found
     */
    @PatchMapping(value = "/{accountId}/restore")
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> restoreCheckingAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(mapToAccountProfileResponse(checkingAccountService.restoreCheckingAccount(accountId)));
    }

    // AUXILIARY

    /**
     * Maps a CheckingAccount entity to an AccountProfileResponse DTO.
     *
     * @param account the CheckingAccount entity to be mapped
     * @return an AccountProfileResponse DTO containing the relevant account information
     */
    private AccountProfileResponse mapToAccountProfileResponse(CheckingAccount account) {
        return new AccountProfileResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getOpeningDate(),
                account.getStatus()
        );
    }

    /**
     * Converts a list of CheckingAccount entities to a list of AccountProfileResponse DTOs.
     *
     * @param accounts the list of CheckingAccount entities to be converted
     * @return a list of AccountProfileResponse DTOs representing the checking accounts
     */
    private List<AccountProfileResponse> toProfiles(List<CheckingAccount> accounts) {
        return accounts.stream()
                .map(this::mapToAccountProfileResponse)
                .toList();
    }


}
