package com.tuon.tbank.controller.account;

import com.tuon.tbank.dto.account.*;
import com.tuon.tbank.entity.account.JointAccount;
import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.service.account.AccountService;
import com.tuon.tbank.service.account.JointAccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/accounts/joint-accounts")
public class JointAccountController {

    private final JointAccountService jointAccountService;
    private final AccountService accountService;


    public JointAccountController(JointAccountService jointAccountService, AccountService accountService) {
        this.jointAccountService = jointAccountService;
        this.accountService = accountService;
    }

    /**
     * Retrieves all joint accounts.
     *
     * @return a ResponseEntity containing a list of AccountProfileResponse DTOs representing the joint accounts
     */
    @GetMapping
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<List<AccountProfileResponse>> getAllJointAccounts() {
        List<AccountProfileResponse> response = toProfiles(jointAccountService.getAllJointAccounts());
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    /**
     * Retrieves a joint account by its ID.
     *
     * @param accountId the ID of the joint account to be retrieved
     * @return a ResponseEntity containing an AccountProfileResponse DTO representing the joint account
     */
    @GetMapping("/{accountId}")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> getJointAccountById(@PathVariable Long accountId) {
        JointAccount account = jointAccountService.getJointAccountById(accountId);
        return ResponseEntity.ok(mapToAccountProfileResponse(account));
    }

    /**
     * Retrieves a joint account by its account number.
     *
     * @param accountNumber the account number of the joint account to be retrieved
     * @return a ResponseEntity containing an AccountProfileResponse DTO representing the joint account
     */
    @GetMapping("/search/number")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> getJointAccountByAccountNumber(@RequestParam String accountNumber) {
        JointAccount account = jointAccountService.getJointAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(mapToAccountProfileResponse(account));
    }

    /**
     * Retrieves joint accounts by a list of owner IDs.
     *
     * @param ownerIds the list of owner IDs to search for joint accounts
     * @return a ResponseEntity containing a list of AccountProfileResponse DTOs representing the joint accounts
     */
    @GetMapping("/search/owners")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<List<AccountProfileResponse>> getJointAccountsByOwnersId(
            @RequestParam("ownerIds") List<Long> ownerIds) {

        List<AccountProfileResponse> response =
                toProfiles(jointAccountService.getJointAccountsByOwnersId(ownerIds));

        return response.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(response);
    }

    /**
     * Retrieves joint accounts by a single owner ID.
     *
     * @param ownerId the owner ID to search for joint accounts
     * @return a ResponseEntity containing a list of AccountProfileResponse DTOs representing the joint accounts
     */
    @GetMapping("/search/owners/ownerId")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<List<AccountProfileResponse>> getJointAccountsByOwnerId(
            @RequestParam("ownerId") Long ownerId) {

        List<AccountProfileResponse> response =
                toProfiles(jointAccountService.getJointAccountsByOwnerId(ownerId));

        return response.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(response);
    }

    /**
     * Retrieves a joint account by its ID and owner ID.
     *
     * @param id      the ID of the joint account to be retrieved
     * @param ownerId the owner ID associated with the joint account
     * @return a ResponseEntity containing an AccountProfileResponse DTO representing the joint account
     */
    @GetMapping("/search/{id}/owners/{ownerId}")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> getJointAccountByIdAndOwnerId(@PathVariable Long id, @PathVariable Long ownerId) {

        JointAccount account = jointAccountService.getJointAccountByIdAndOwnerId(id, ownerId);
        return ResponseEntity.ok(mapToAccountProfileResponse(account));
    }

    /**
     * Retrieves the balance of a joint account by its ID.
     *
     * @param accountId the ID of the joint account whose balance is to be retrieved
     * @return a ResponseEntity containing an AccountBalanceResponse DTO representing the joint account balance
     */
    @GetMapping("/{accountId}/balance")
    @PreAuthorize("@accountAuthorization.canAccessJointAccount(#accountId, authentication)")
    public ResponseEntity<AccountBalanceResponse> getJointAccountBalance(@PathVariable Long accountId) {
        AccountBalanceResponse balance = jointAccountService.getJointAccountBalance(accountId);
        return ResponseEntity.ok(balance);
    }

    /**
     * Retrieves all deleted joint accounts.
     *
     * @return a ResponseEntity containing a list of AccountProfileResponse DTOs representing the deleted joint accounts
     */
    @GetMapping("/search/deleted")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<List<AccountProfileResponse>> getAllDeletedJointAccounts() {
        List<AccountProfileResponse> response = toProfiles(jointAccountService.getAllDeletedJointAccounts());
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    /**
     * Retrieves a deleted joint account by its ID.
     *
     * @param accountId the ID of the deleted joint account to be retrieved
     * @return a ResponseEntity containing an AccountProfileResponse DTO representing the deleted joint account
     */
    @GetMapping("/search/deleted/{accountId}")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> getDeletedJointAccountById(@PathVariable Long accountId) {
        JointAccount account = jointAccountService.getDeletedJointAccountById(accountId);
        return ResponseEntity.ok(mapToAccountProfileResponse(account));
    }

    /**
     * Retrieves a deleted joint account by its account number.
     *
     * @param accountNumber the account number of the deleted joint account to be retrieved
     * @return a ResponseEntity containing an AccountProfileResponse DTO representing the deleted joint account
     */
    @GetMapping("/search/deleted/number")
    @PreAuthorize("@accountAuthorization.canSearchAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> getDeletedJointAccountByAccountNumber(@RequestParam String accountNumber) {
        JointAccount account = jointAccountService.getDeletedJointAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(mapToAccountProfileResponse(account));
    }

    /**
     * Creates a new joint account with the specified owners.
     *
     * @param request the JointAccountCreateRequest containing the owner IDs for the new joint account
     * @return a ResponseEntity containing a JointAccountCreateResponse DTO representing the created joint account
     */
    @PostMapping
    @PreAuthorize("@accountAuthorization.canOpenJointAccountFor(#request.ownerIds, authentication)")
    public ResponseEntity<JointAccountCreateResponse> insertJointAccount (@Valid @RequestBody JointAccountCreateRequest request) {
        JointAccount jointAccount = jointAccountService.registerJointAccount(request.owner1Id(), request.owner2Id());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(jointAccount.getId()).toUri();
        return ResponseEntity.created(uri).body(new JointAccountCreateResponse(
                jointAccount.getId(),
                jointAccount.getAccountNumber(),
                jointAccount.getOpeningDate(),
                jointAccount.getOwners().stream().findFirst().orElseThrow(() -> new IllegalStateException("Owner 1 not found")),
                jointAccount.getOwners().stream().skip(1).findFirst().orElseThrow(() -> new IllegalStateException("Owner 2 not found"))
        ));
    }

    /**
     * Blocks a joint account by its ID.
     *
     * @param accountId the ID of the joint account to be blocked
     * @param request   the AccountStatusUpdateRequest containing the account number for verification
     * @return a ResponseEntity containing an AccountProfileResponse DTO representing the blocked joint account
     */
    @PatchMapping("/{accountId}/block")
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> blockJointAccount(@PathVariable Long accountId,
                                                                    @Valid @RequestBody AccountStatusUpdateRequest request) {
        return ResponseEntity.ok(mapToAccountProfileResponse(jointAccountService.blockJointAccount(accountId, request.accountNumber())));
    }

    /**
     * Suspends a joint account by its ID.
     *
     * @param accountId the ID of the joint account to be suspended
     * @param request   the AccountStatusUpdateRequest containing the account number for verification
     * @return a ResponseEntity containing an AccountProfileResponse DTO representing the suspended joint account
     */
    @PatchMapping("/{accountId}/suspend")
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> suspendJointAccount (@PathVariable Long accountId,
                                                                       @Valid @RequestBody AccountStatusUpdateRequest request) {
        return ResponseEntity.ok(mapToAccountProfileResponse(jointAccountService.suspendJointAccount(accountId, request.accountNumber())));
    }

    /**
     * Reactivates a joint account by its ID.
     *
     * @param accountId the ID of the joint account to be reactivated
     * @param request   the AccountStatusUpdateRequest containing the account number for verification
     * @return a ResponseEntity containing an AccountProfileResponse DTO representing the reactivated joint account
     */
    @PatchMapping("/{accountId}/reactivate")
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> reactivateJointAccount (@PathVariable Long accountId,
                                                                          @Valid @RequestBody AccountStatusUpdateRequest request) {
        return ResponseEntity.ok(mapToAccountProfileResponse(jointAccountService.reactivateJointAccount(accountId, request.accountNumber())));
    }

    /**
     * Updates the owners of a joint account by its ID.
     *
     * @param accountId the ID of the joint account to be updated
     * @param ownersId  the set of owner IDs to be associated with the joint account
     * @return a ResponseEntity containing an AccountProfileResponse DTO representing the updated joint account
     */
    @PutMapping("/{accountId}/owners/{ownersId}")
    @PreAuthorize("@accountAuthorization.canAdministerAccount(authentication)")
    public ResponseEntity <AccountProfileResponse> updateJointAccountOwners(@PathVariable Long accountId, @PathVariable Collection<Long> ownersId) {
        JointAccount updatedAccountOwners = jointAccountService.updateJointAccountOwners(accountId, ownersId);
        return ResponseEntity.ok(mapToAccountProfileResponse(updatedAccountOwners));
    }

    /**
     * Deletes a joint account by its ID.
     *
     * @param accountId the ID of the joint account to be deleted
     * @return a ResponseEntity with no content, indicating successful deletion
     */
    @DeleteMapping(value = "/{accountId}")
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public ResponseEntity<Void> deleteJointAccount(@PathVariable Long accountId) {
        jointAccountService.deleteJointAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Restores a deleted joint account by its ID.
     *
     * @param accountId the ID of the joint account to be restored
     * @return a ResponseEntity containing an AccountProfileResponse DTO representing the restored joint account
     */
    @PatchMapping(value = "/{accountId}/restore")
    @PreAuthorize("@accountAuthorization.canManageAccount(authentication)")
    public ResponseEntity<AccountProfileResponse> restoreJointAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(mapToAccountProfileResponse(jointAccountService.restoreJointAccount(accountId)));
    }

    // AUXILIARY

    /**
     * Maps a CheckingAccount entity to an AccountProfileResponse DTO.
     *
     * @param account the CheckingAccount entity to be mapped
     * @return an AccountProfileResponse DTO containing the relevant account information
     */
    private AccountProfileResponse mapToAccountProfileResponse(JointAccount account) {
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
    private List<AccountProfileResponse> toProfiles(List<JointAccount> accounts) {
        return accounts.stream()
                .map(this::mapToAccountProfileResponse)
                .toList();
    }
}
