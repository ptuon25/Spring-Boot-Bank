package com.tuon.tbank.repository.account;

import com.tuon.tbank.entity.account.BusinessAccount;
import com.tuon.tbank.enums.company.CompanyMemberRole;
import com.tuon.tbank.enums.company.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Provides database access operations for business accounts.
 */
public interface BusinessAccountRepository extends JpaRepository<BusinessAccount, Long> {


    /**
     * Retrieves a business account by its owner ID and account ID.
     *
     * @param ownerId   the ID of the owner (company)
     * @param accountId the ID of the business account
     * @return an {@link Optional} containing the found business account, or empty if not found
     */
    Optional<BusinessAccount> findByOwnerIdAndIdAndDeletedFalseAndOwnerDeletedFalse(
            Long ownerId,
            Long accountId);

    /**
     * Retrieves a list of business accounts that are accessible by a specific client, based on their membership status and roles.
     *
     * @param clientId the ID of the client
     * @param status   the membership status to check
     * @param roles    the collection of roles to check
     * @return a list of business accounts accessible by the specified client
     */
    @Query("""
            select account
            from BusinessAccount account
            where account.owner.deleted = false
              and account.deleted = false
              and (
                  account.owner.partner.id = :clientId
                  or exists (
                      select membership.id
                      from CompanyMembership membership
                      where membership.company = account.owner
                        and membership.client.id = :clientId
                        and membership.status = :status
                        and membership.role in :roles
                  )
              )
            order by account.id
            """)
    List<BusinessAccount> findAccessibleByClient(
            @Param("clientId") Long clientId,
            @Param("status") MembershipStatus status,
            @Param("roles") Collection<CompanyMemberRole> roles);

    /**
     * Retrieves a list of business accounts by the owner's ID, ensuring the owner is not marked as deleted.
     *
     * @param companyId the ID of the company (owner)
     * @return a list of business accounts with the specified owner ID
     */
    List<BusinessAccount> findByOwnerIdAndOwnerDeletedFalseAndDeletedFalse(Long companyId);

    /**
     * Retrieves all business accounts that are not marked as deleted.
     *
     * @return a list of active business accounts
     */
    List<BusinessAccount> findByDeletedFalse();

    /**
     * Retrieves a non-deleted business account by its ID.
     *
     * @param accountId the ID of the business account
     * @return an {@link Optional} containing the found account, or empty if not found
     */
    Optional<BusinessAccount> findByIdAndDeletedFalse(Long accountId);

    Optional<BusinessAccount> findByIdAndDeletedTrue(Long accountId);

    /**
     * Retrieves a business account by its account number, ensuring it is not marked as deleted.
     *
     * @param accountNumber the account number to search for
     * @return an {@link Optional} containing the found business account, or empty if not found
     */
    Optional<BusinessAccount> findByAccountNumberAndDeletedFalse(String accountNumber);

    /**
     * Checks if a business account is accessible by a specific client.
     *
     * @param accountId the ID of the business account
     * @param clientId  the ID of the client
     * @param status    the membership status to check
     * @param roles     the collection of roles to check
     * @return true if the business account is accessible by the client, false otherwise
     */
    @Query("""
            select count(account) > 0
            from BusinessAccount account
            where account.id = :accountId
              and account.deleted = false
              and account.owner.deleted = false
              and (
                  account.owner.partner.id = :clientId
                  or exists (
                      select membership.id
                      from CompanyMembership membership
                      where membership.company = account.owner
                        and membership.client.id = :clientId
                        and membership.status = :status
                        and membership.role in :roles
                  )
              )
            """)
    boolean existsAccessibleByClient(
            @Param("accountId") Long accountId,
            @Param("clientId") Long clientId,
            @Param("status") MembershipStatus status,
            @Param("roles") Collection<CompanyMemberRole> roles);
}
