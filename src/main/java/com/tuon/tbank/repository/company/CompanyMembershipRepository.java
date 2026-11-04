package com.tuon.tbank.repository.company;

import com.tuon.tbank.entity.company.CompanyMembership;
import com.tuon.tbank.enums.company.CompanyMemberRole;
import com.tuon.tbank.enums.company.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
     * Finds the membership for a client in a company, regardless of its status.
     * This is required to reactivate INACTIVE or REVOKED memberships.
     */
    Optional<CompanyMembership> findByCompanyIdAndClientId(Long companyId, Long clientId);

    /**
     * Lists memberships belonging to a company with the requested status.
     */
    List<CompanyMembership> findByCompanyIdAndStatus(Long companyId, MembershipStatus status);

    /**
     * Finds a membership only when it belongs to the requested company.
     */
    Optional<CompanyMembership> findByIdAndCompanyId(Long membershipId, Long companyId);

    /**
     * Checks if a membership exists for a given company, client, status, and role.
     *
     * @param companyId the ID of the company
     * @param clientId  the ID of the client
     * @param status    the status of the membership
     * @param roles     the roles of the membership
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
     * @param membershipId     the ID of the membership
     * @param userId           the ID of the user
     * @param membershipStatus the status of the membership
     * @return true if a membership exists, false otherwise
     */
    boolean existsByIdAndClientIdAndStatus(Long membershipId, Long userId, MembershipStatus membershipStatus);

    /**
     * Checks if a membership exists for a given company and client.
     *
     * @param companyId the ID of the company
     * @param clientId  the ID of the client
     * @return true if a membership exists, false otherwise
     */
    boolean existsByCompanyIdAndClientId(Long companyId, Long clientId);

    /**
     * Checks if a membership exists for a given ID, client, status, and role.
     *
     * @param membershipId     the ID of the membership
     * @param userId           the ID of the user
     * @param membershipStatus the status of the membership
     * @param admin            the roles of the membership
     * @return true if a membership exists, false otherwise
     */
    boolean existsByIdAndClientIdAndStatusAndRoleIn(Long membershipId, Long userId, MembershipStatus membershipStatus, List<CompanyMemberRole> admin);
}
