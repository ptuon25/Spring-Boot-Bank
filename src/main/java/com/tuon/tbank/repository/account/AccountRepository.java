package com.tuon.tbank.repository.account;

import com.tuon.tbank.entity.account.Account;
import com.tuon.tbank.enums.account.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Provides the data access operations for the {@link Account} entity, providing persistence operations
 * and database queries.
 *
 * <p>Extends {@link JpaRepository}, inheriting standard CRUD operations, such as
 * saving, retrieving by ID, and deleting records.</p>
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Retrieves a list of accounts that are marked as deleted.
     *
     * @return a list of deleted accounts
     */
    List<Account> findByDeletedTrue();

    /**
     * Retrieves a list of accounts that are not marked as deleted.
     *
     * @return a list of non-deleted accounts
     */
    Optional<Account> findByIdAndDeletedFalse(Long id);

    /**
     * Retrieves an account by its account number, ensuring it is not marked as deleted.
     *
     * @param number the account number to search for
     * @return an {@link Optional} containing the found account, or empty if not found
     */
    @Query("""
                select account
                from Account account
                where account.accountNumber like concat('%-', :number)
                  and account.deleted = false
            """)
    List<Account> findByNumericAccountNumber(@Param("number") String number);

    /**
     * Retrieves an account by its account number, ensuring it is not marked as deleted.
     *
     * @param accountNumber the account number to search for
     * @return an {@link Optional} containing the found account, or empty if not found
     */
    Optional<Account> findByAccountNumberAndDeletedFalse(String accountNumber);

    /**
     * Retrieves a list of accounts that were opened between the specified start and end dates, ensuring they are not marked as deleted.
     *
     * @param startDate the start date of the opening date range
     * @param endDate   the end date of the opening date range
     * @return a list of accounts opened within the specified date range
     */
    List<Account> findByOpeningDateBetweenAndDeletedFalse(LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves a list of accounts with the specified status, ensuring they are not marked as deleted.
     *
     * @param status the account status to search for
     * @return a list of accounts with the specified status
     */
    List<Account> findByStatusAndDeletedFalse(AccountStatus status);

    /**
     * Retrieves a list of accounts that are not marked as deleted.
     *
     * @return a list of non-deleted accounts
     */
    List<Account> findByDeletedFalse();

    /**
     * Retrieves an account by its ID, ensuring it is marked as deleted.
     *
     * @param id the ID of the account to search for
     * @return an {@link Optional} containing the found account, or empty if not found
     */
    Optional<Account> findByIdAndDeletedTrue(Long id);

    /**
     * Retrieves an account by its account number, ensuring it is marked as deleted.
     *
     * @param accountNumber the account number to search for
     * @return an {@link Optional} containing the found account, or empty if not found
     */
    @Query("""
                select account
                from Account account
                where account.accountNumber like concat('%-', :number)
                  and account.deleted = false
            """)
    Optional<Account> findByAccountNumberAndDeletedTrue(String accountNumber);

    /**
     * Retrieves a list of accounts that were opened between the specified start and end dates, ensuring they are marked as deleted.
     *
     * @param startDate the start date of the opening date range
     * @param endDate   the end date of the opening date range
     * @return a list of accounts opened within the specified date range
     */
    List<Account> findByOpeningDateBetweenAndDeletedTrue(LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves a list of accounts with the specified status, ensuring they are marked as deleted.
     *
     * @param status the account status to search for
     * @return a list of accounts with the specified status
     */
    List<Account> findByStatusAndDeletedTrue(AccountStatus status);

    /**
     * Checks if an account exists with the specified account number.
     *
     * @param accountNumber the account number to check for existence
     * @return true if an account with the specified account number exists, false otherwise
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Checks if an account exists with the specified account number, excluding the account with the given ID.
     *
     * @param accountNumber the account number to check for existence
     * @param id            the ID of the account to exclude from the check
     * @return true if an account with the specified account number exists (excluding the specified ID), false otherwise
     */
    boolean existsByAccountNumberAndIdNot(String accountNumber, Long id);

    /**
     * Retrieves the next available account number for creating a new account.
     *
     * @return the next available account number as a Long value
     */
    @Query(value = "SELECT nextval('account_number_sequence')", nativeQuery = true)
    Long getNextAccountNumber();
}
