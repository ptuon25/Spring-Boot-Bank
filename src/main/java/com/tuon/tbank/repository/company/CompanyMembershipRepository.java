package com.tuon.tbank.repository.company;

import com.tuon.tbank.entity.company.CompanyMembership;
import com.tuon.tbank.enums.company.CompanyMemberRole;
import com.tuon.tbank.enums.company.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

/**
 * Provides access to client memberships in companies.
 *
 * <p>This repository allows for the management of client memberships in companies,
 * including checking for the existence of memberships based on company ID, client ID,
 * status, and role.
 * </p>
 */
public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership, Long> {

    /**
     * Checks if a membership exists for a given company, client, status, and role.
     *
     * @param companyId the ID of the company
     * @param clientId the ID of the client
     * @param status the status of the membership
     * @param roles the roles of the membership
     * @return true if a membership exists, false otherwise
     */
    boolean existsByCompanyIdAndClientIdAndStatusAndRoleIn(
            Long companyId,
            Long clientId,
            MembershipStatus status,
            Collection<CompanyMemberRole> roles);

    /**
     * Checks if a membership exists for a given ID, client, and status.
     *
     * @param membershipId the ID of the membership
     * @param userId the ID of the user
     * @param membershipStatus the status of the membership
     * @return true if a membership exists, false otherwise
     */
    boolean existsByIdAndClientIdAndStatus(Long membershipId, Long userId, MembershipStatus membershipStatus);
}
