package com.tuon.tbank.controller.account;

import com.tuon.tbank.dto.account.AccountProfileResponse;
import com.tuon.tbank.entity.account.Account;
import com.tuon.tbank.enums.account.AccountStatus;
import com.tuon.tbank.service.account.AccountService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing accounts, providing endpoints for account-related operations.
 *
 * <p>This controller handles HTTP requests related to account management and delegates business logic to the {@link AccountService}.</p>
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    /**
     * The service responsible for account-related business logic.
     */
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Retrieves all accounts.
     *
     * @return a ResponseEntity containing a list of AccountProfileResponse objects or no content if none found
     */
    @GetMapping
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<List<AccountProfileResponse>> getAllAccounts() {
        List<AccountProfileResponse> response = toProfiles(accountService.getAllAccounts());
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    /**
     * Retrieves all valid (non-deleted) accounts.
     *
     * @return a ResponseEntity containing a list of AccountProfileResponse objects or no content if none found
     */
    @GetMapping("/valid")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<List<AccountProfileResponse>> getAllValidAccounts() {
        List<AccountProfileResponse> response = toProfiles(accountService.getAllValidAccounts());
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    /**
     * Retrieves all deleted accounts.
     *
     * @return a ResponseEntity containing a list of AccountProfileResponse objects or no content if none found
     */
    @GetMapping("/deleted")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<List<AccountProfileResponse>> getAllDeletedAccounts() {
        List<AccountProfileResponse> response = toProfiles(accountService.getAllDeletedAccounts());
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    /**
     * Retrieves an account by its ID.
     *
     * @param id the ID of the account to retrieve
     * @return a ResponseEntity containing the AccountProfileResponse object or not found if the account does not exist
     */
    @GetMapping("/{id}")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> getAccountById(@PathVariable Long id) {
        Account account = accountService.getAccountById(id);
        return ResponseEntity.ok(mapToAccountProfileResponse(account));
    }

    /**
     * Retrieves an account by its account number.
     *
     * @param accountNumber the account number of the account to retrieve
     * @return a ResponseEntity containing the AccountProfileResponse object or not found if the account does not exist
     */
    @GetMapping("/search/{accountNumber}")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> getAccountByAccountNumber(String accountNumber) {
        return ResponseEntity.ok(mapToAccountProfileResponse(accountService.getAccountByAccountNumber(accountNumber)));
    }

    /**
     * Retrieves accounts by their opening date range.
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return a ResponseEntity containing a list of AccountProfileResponse objects or no content if none found
     */
    @GetMapping("/search/opening-date-range")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<List<AccountProfileResponse>> getAccountByOpeningDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(toProfiles(accountService.getAccountByOpeningDateRange(startDate, endDate)));
    }

    /**
     * Retrieves accounts by their status.
     *
     * @param status the status of the accounts to retrieve
     * @return a ResponseEntity containing a list of AccountProfileResponse objects or no content if none found
     */
    @GetMapping("/search/status")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<List<AccountProfileResponse>> getAccountByStatus(@RequestParam AccountStatus status) {
        return ResponseEntity.ok(toProfiles(accountService.getAccountByStatus(status)));
    }

    /**
     * Retrieves a deleted account by its ID.
     *
     * @param id the ID of the deleted account to retrieve
     * @return a ResponseEntity containing the AccountProfileResponse object or not found if the account does not exist
     */
    @GetMapping("/search/deleted/{id}")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> getDeletedAccountById(@PathVariable Long id) {
        Account account = accountService.getByIdAndDeletedTrue(id);
        return ResponseEntity.ok(mapToAccountProfileResponse(account));
    }

    /**
     * Retrieves a deleted account by its account number.
     *
     * @param accountNumber the account number of the deleted account to retrieve
     * @return a ResponseEntity containing the AccountProfileResponse object or not found if the account does not exist
     */
    @GetMapping("/search/deleted/account-number")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> getDeletedAccountByAccountNumber(@RequestParam String accountNumber) {
        Account account = accountService.getByAccountNumberAndDeletedTrue(accountNumber);
        return ResponseEntity.ok(mapToAccountProfileResponse(account));
    }

    /**
     * Retrieves all deleted accounts within a specified date range.
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return a ResponseEntity containing a list of AccountProfileResponse objects or no content if none found
     */
    @GetMapping("/search/deleted/opening-date-range")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<List<AccountProfileResponse>> getDeletedAccountByOpeningDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Account> accounts = accountService.getAccountByOpeningDateRangeAndDeletedTrue(startDate, endDate);
        return ResponseEntity.ok(toProfiles(accounts));
    }

    /**
     * Retrieves all deleted accounts with a specified status.
     *
     * @param status the status of the deleted accounts to retrieve
     * @return a ResponseEntity containing a list of AccountProfileResponse objects or no content if none found
     */
    @GetMapping("/search/deleted/status")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<List<AccountProfileResponse>> getDeletedAccountByStatus(@RequestParam AccountStatus status) {
        List<Account> accounts = accountService.getAccountByStatusAndDeletedTrue(status);
        return ResponseEntity.ok(toProfiles(accounts));
    }

    // AUXILIARY

    private AccountProfileResponse mapToAccountProfileResponse(Account account) {
        return new AccountProfileResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getOpeningDate(),
                account.getStatus()
        );
    }

    private List<AccountProfileResponse> toProfiles(List<Account> accounts) {
        return accounts.stream()
                .map(this::mapToAccountProfileResponse)
                .toList();
    }

}
