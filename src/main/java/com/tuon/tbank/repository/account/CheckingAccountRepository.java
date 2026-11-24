package com.tuon.tbank.repository.account;

import com.tuon.tbank.entity.account.CheckingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Provides database access operations for checking accounts.
 */
public interface CheckingAccountRepository extends JpaRepository<CheckingAccount, Long> {

    /**
     * Retrieves a checking account by its owner ID, ensuring it is not marked as deleted.
     *
     * @param ownerId the ID of the owner
     * @return an {@link Optional} containing the found checking account, or empty if not found
     */
    List<CheckingAccount> findByOwnerIdAndDeletedFalse(Long ownerId);

    /**
     * Retrieves a checking account by its ID and owner ID, ensuring it is not marked as deleted.
     *
     * @param accountId the ID of the checking account
     * @param ownerId   the ID of the owner of the checking account
     * @return an {@link Optional} containing the found checking account, or empty if not found
     */
    Optional<CheckingAccount> findByIdAndOwnerIdAndDeletedFalse(Long accountId, Long ownerId);

    /**
     * Retrieves a list of checking accounts that are not marked as deleted.
     *
     * @return a {@link List} of the found checking accounts, or an empty list if none are found
     */
    List<CheckingAccount> findAllByDeletedFalse();

    /**
     * Retrieves a checking account by its ID, ensuring it is not marked as deleted.
     *
     * @param accountId the ID of the checking account
     * @return an {@link Optional} containing the found checking account, or empty if not found
     */
    Optional<CheckingAccount> findByIdAndDeletedFalse(Long accountId);

    /**
     * Retrieves a checking account by its account number, ensuring it is not marked as deleted.
     *
     * @param accountNumber the account number of the checking account
     * @return an {@link Optional} containing the found checking account, or empty if not found
     */
    Optional<CheckingAccount> findByAccountNumberAndDeletedFalse(String accountNumber);

    /**
     * Retrieves a list of checking accounts that are marked as deleted.
     *
     * @return a {@link List} of the found checking accounts, or an empty list if none are found
     */
    List<CheckingAccount> findAllByDeletedTrue();

    /**
     * Retrieves a checking account by its ID, ensuring it is marked as deleted.
     *
     * @param accountId the ID of the checking account
     * @return an {@link Optional} containing the found checking account, or empty if not found
     */
    Optional<CheckingAccount> findByIdAndDeletedTrue(Long accountId);

    /**
     * Checks if a checking account exists with the given account ID and owner ID, and that the owner is not deleted.
     *
     * @param accountId the ID of the checking account
     * @param ownerId   the ID of the owner of the checking account
     * @return true if such a checking account exists and the owner is not deleted, false otherwise
     */
    boolean existsByIdAndOwnerIdAndOwnerDeletedFalse(Long accountId, Long ownerId);
}
