package com.tuon.tbank.repository.account;

import com.tuon.tbank.entity.account.JointAccount;
import com.tuon.tbank.enums.person.ClientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Provides database access operations for joint accounts.
 */
public interface JointAccountRepository extends JpaRepository<JointAccount, Long> {

    List<JointAccount> findAllByDeletedFalse();

    Optional<JointAccount> findByIdAndDeletedFalse(Long id);

    /**
     * Retrieves a list of joint accounts that have owners with the specified IDs, ensuring they are not marked as deleted.
     *
     * @param ownerIds a collection of owner IDs to search for
     * @return a list of joint accounts with the specified owner IDs
     */
    List<JointAccount> findByOwnersIdInAndDeletedFalse(Collection<Long> ownerIds);

    /**
     * Retrieves a joint account by its ID and a specific owner ID, ensuring the account is not marked as deleted.
     *
     * @param clientId the ID of the owner
     * @return a list of joint accounts with the specified owner ID
     */
    @Query("""
                select account
                from JointAccount account
                join account.owners owner
                where owner.id = :clientId
                  and account.deleted = false
            """)
    List<JointAccount> findByOwnerId(@Param("clientId") Long clientId);

    /**
     * Retrieves a joint account by its ID and a specific owner ID, ensuring the account is not marked as deleted.
     *
     * @param accountId the ID of the joint account
     * @param ownerId   the ID of the owner
     * @return an {@link Optional} containing the found joint account, or empty if not found
     */
    Optional<JointAccount> findByIdAndOwners_IdAndDeletedFalse(Long accountId, Long ownerId);

    /**
     * Retrieves a joint account by its account number, ensuring it is not marked as deleted.
     *
     * @param accountNumber the account number of the joint account
     * @return an {@link Optional} containing the found joint account, or empty if not found
     */
    Optional<JointAccount> findByAccountNumberAndDeletedFalse(String accountNumber);

    /**
     * Retrieves a list of joint accounts that are marked as deleted.
     *
     * @return a {@link List} of the found joint accounts, or an empty list if none are found
     */
    List<JointAccount> findAllByDeletedTrue();

    /**
     * Retrieves a joint account by its ID, ensuring it is marked as deleted.
     *
     * @param id the ID of the joint account
     * @return an {@link Optional} containing the found joint account, or empty if not found
     */
    Optional<JointAccount> findByIdAndDeletedTrue(Long id);

    /**
     * Retrieves a joint account by its account number, ensuring it is marked as deleted.
     *
     * @param accountNumber the account number of the joint account
     * @return an {@link Optional} containing the found joint account, or empty if not found
     */
    Optional<JointAccount> findByAccountNumberAndDeletedTrue(String accountNumber);


    /**
     * Checks if a joint account is accessible by a specific client.
     *
     * @param accountId the ID of the joint account
     * @param ownerId   the ID of the owner
     * @return true if the joint account is accessible by the client, false otherwise
     */
    @Query("""
    select count(a) > 0 from JointAccount a join a.owners o
    where a.id = :accountId and o.id = :ownerId
      and o.deleted = false and a.deleted = false
    """)
    boolean existsByAccountIdAndOwnerId(@Param("accountId") Long accountId,
                                        @Param("ownerId") Long ownerId);
}
