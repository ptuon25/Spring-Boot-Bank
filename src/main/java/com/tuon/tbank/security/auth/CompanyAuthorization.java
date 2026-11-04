package com.tuon.tbank.security.auth;

import com.tuon.tbank.enums.auth.UserIdentityType;
import com.tuon.tbank.enums.company.CompanyMemberRole;
import com.tuon.tbank.enums.company.MembershipStatus;
import com.tuon.tbank.repository.account.BusinessAccountRepository;
import com.tuon.tbank.repository.company.CompanyRepository;
import com.tuon.tbank.repository.company.CompanyMembershipRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Provides authorization checks for company-related operations.
 *
 * <p>This class contains methods to determine whether the authenticated user
 * has the necessary permissions to access company data and business accounts.</p>
 */
@Component("companyAuthorization")
public class CompanyAuthorization {

    private final CompanyRepository companyRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final BusinessAccountRepository businessAccountRepository;

    /**
     * Constructs a new instance of {@code CompanyAuthorization} with the specified repositories.
     *
     * @param companyRepository         the repository for accessing company data
     * @param membershipRepository      the repository for accessing company membership data
     * @param businessAccountRepository the repository for accessing business account data
     */
    public CompanyAuthorization(CompanyRepository companyRepository,
                                CompanyMembershipRepository membershipRepository,
                                BusinessAccountRepository businessAccountRepository) {
        this.companyRepository = companyRepository;
        this.membershipRepository = membershipRepository;
        this.businessAccountRepository = businessAccountRepository;
    }

    /**
     * Checks if the authenticated user can read the specified company.
     *
     * @param companyId      the ID of the company to check
     * @param authentication the authentication information
     * @return true if the user can read the company, false otherwise
     */
    public boolean canReadCompany(Long companyId, Authentication authentication) {
        if (companyId == null
                || authentication == null
                || !authentication.isAuthenticated()
                || !(authentication instanceof JwtAuthenticationToken jwt)) {
            return false;
        }

        String identityType = jwt.getToken().getClaimAsString("identity_type");
        Long userId = getUserId(jwt);

        boolean isEmployee = UserIdentityType.EMPLOYEE.name().equals(identityType);
        boolean isPrivilegedEmployee = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_MANAGER")
                                || authority.getAuthority().equals("ROLE_ADMIN"));

        if (isEmployee) {
            return isPrivilegedEmployee
                    || (userId != null
                    && companyRepository.existsByIdAndPartnerIdAndDeletedFalse(companyId, userId));
        }

        if (!UserIdentityType.CLIENT.name().equals(identityType) || userId == null) {
            return false;
        }

        return companyRepository.existsByIdAndPartnerIdAndDeletedFalse(companyId, userId)
                || membershipRepository.existsByCompanyIdAndClientIdAndStatusAndRoleIn(
                companyId,
                userId,
                MembershipStatus.ACTIVE,
                List.of(CompanyMemberRole.ADMIN,
                        CompanyMemberRole.OPERATOR, CompanyMemberRole.VIEWER));
    }

    /**
     * Determines whether the authenticated user can register a company.
     *
     * @param authentication the current authentication information
     * @return {@code true} for an authenticated client or a manager/admin employee
     */
    public boolean canRegisterCompany(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return false;
        }

        JwtAuthenticationToken jwt = (JwtAuthenticationToken) authentication;
        String identityType = jwt.getToken().getClaimAsString("identity_type");
        if (UserIdentityType.CLIENT.name().equals(identityType)) {
            return true;
        }
        return UserIdentityType.EMPLOYEE.name().equals(identityType)
                && (hasAuthority(authentication, "ROLE_MANAGER")
                || hasAuthority(authentication, "ROLE_ADMIN"));
    }

    /**
     * Checks whether the authenticated client can access a business account.
     * The account is filtered by its owning company in the database.
     */
    public boolean canReadBusinessAccount(Long accountId, Authentication authentication) {
        if (accountId == null || !isAuthenticated(authentication)) {
            return false;
        }

        JwtAuthenticationToken jwt = (JwtAuthenticationToken) authentication;
        String identityType = jwt.getToken().getClaimAsString("identity_type");
        if (UserIdentityType.EMPLOYEE.name().equals(identityType)) {
            return hasAuthority(authentication, "ROLE_ASSOCIATE")|| hasAuthority(authentication, "ROLE_MANAGER")
                    || hasAuthority(authentication, "ROLE_ADMIN");
        }

        Long clientId = getUserId(jwt);
        return UserIdentityType.CLIENT.name().equals(identityType)
                && clientId != null
                && businessAccountRepository.existsAccessibleByClient(
                accountId,
                clientId,
                MembershipStatus.ACTIVE,
                List.of(CompanyMemberRole.ADMIN,
                        CompanyMemberRole.OPERATOR, CompanyMemberRole.VIEWER));
    }

    /**
     * Checks if the authenticated user can read the specified company membership.
     *
     * @param membershipId   the ID of the company membership to check
     * @param authentication the authentication information
     * @return true if the user can read the company membership, false otherwise
     */
    private boolean canReadCompanyMembership(Long membershipId, Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return false;
        }

        JwtAuthenticationToken jwt = (JwtAuthenticationToken) authentication;
        String identityType = jwt.getToken().getClaimAsString("identity_type");
        Long userId = getUserId(jwt);

        if (!UserIdentityType.CLIENT.name().equals(identityType) || userId == null) {
            return false;
        }

        return membershipRepository.existsByIdAndClientIdAndStatus(
                membershipId,
                userId,
                MembershipStatus.ACTIVE);
    }

    /**
     * Checks if the authenticated user is properly authenticated.
     *
     * @param authentication the authentication information
     * @return true if the user is authenticated, false otherwise
     */
    private boolean isAuthenticated(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication instanceof JwtAuthenticationToken)) {
            return false;
        }
        return true;
    }

    private boolean hasAuthority(Authentication authentication, String requiredAuthority) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> requiredAuthority.equals(authority.getAuthority()));
    }

    private Long getUserId(JwtAuthenticationToken authentication) {
        Object userId = authentication.getToken().getClaim("user_id");
        if (userId == null) {
            return null;
        }
        try {
            return Long.valueOf(userId.toString());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
