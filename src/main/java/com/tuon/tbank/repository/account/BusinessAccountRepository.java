package com.tuon.tbank.repository.account;

import com.tuon.tbank.entity.account.BusinessAccount;
import com.tuon.tbank.enums.company.CompanyMemberRole;
import com.tuon.tbank.enums.company.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

/**
 * Provides database access operations for business accounts.
 */
public interface BusinessAccountRepository extends JpaRepository<BusinessAccount, Long> {

    @Query("""
            select count(account) > 0
            from BusinessAccount account
            where account.id = :accountId
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
