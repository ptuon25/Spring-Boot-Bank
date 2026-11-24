package com.tuon.tbank.security.auth;

import com.tuon.tbank.enums.auth.UserIdentityType;
import com.tuon.tbank.enums.company.CompanyMemberRole;
import com.tuon.tbank.enums.company.MembershipStatus;
import com.tuon.tbank.entity.account.BusinessAccount;
import com.tuon.tbank.repository.account.BusinessAccountRepository;
import com.tuon.tbank.repository.account.CheckingAccountRepository;
import com.tuon.tbank.repository.account.JointAccountRepository;
import com.tuon.tbank.repository.account.SavingsAccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Provides authorization checks for account-related operations.
 *
 * <p>Account ownership is always verified in the database, never through token claims,
 * because a client may own several accounts. Employees are authorized by role.</p>
 */
@Component("accountAuthorization")
public class AccountAuthorization {

    private final CheckingAccountRepository checkingAccountRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final JointAccountRepository jointAccountRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final CompanyAuthorization companyAuthorization;

    /**
     * Creates the account authorization component with the repositories and
     * the company authorization it depends on.
     *
     * @param checkingAccountRepository  the repository used to check checking account ownership
     * @param savingsAccountRepository   the repository used to check savings account ownership
     * @param jointAccountRepository     the repository used to check joint account ownership
     * @param businessAccountRepository  the repository used to check business account access
     * @param companyAuthorization       the component used to check who may act on behalf of a company
     */
    public AccountAuthorization(CheckingAccountRepository checkingAccountRepository,
                                SavingsAccountRepository savingsAccountRepository,
                                JointAccountRepository jointAccountRepository,
                                BusinessAccountRepository businessAccountRepository,
                                CompanyAuthorization companyAuthorization) {
        this.checkingAccountRepository = checkingAccountRepository;
        this.savingsAccountRepository = savingsAccountRepository;
        this.jointAccountRepository = jointAccountRepository;
        this.businessAccountRepository = businessAccountRepository;
        this.companyAuthorization = companyAuthorization;
    }

    // GENERAL (EMPLOYEE ROLES)

    /**
     * Determines whether the authenticated principal can search accounts.
     *
     * @param authentication the current authentication
     * @return {@code true} for an employee with the ASSOCIATE, MANAGER or ADMIN authority;
     * {@code false} otherwise
     */
    public boolean canSearchAccount(Authentication authentication) {
        return isEmployee(authentication)
                && hasAnyAuthority(authentication, "ROLE_ASSOCIATE", "ROLE_MANAGER", "ROLE_ADMIN");
    }

    /**
     * Determines whether the authenticated principal can manage accounts.
     *
     * @param authentication the current authentication
     * @return {@code true} for an employee with the MANAGER or ADMIN authority;
     * {@code false} otherwise
     */
    public boolean canManageAccount(Authentication authentication) {
        return isEmployee(authentication)
                && hasAnyAuthority(authentication, "ROLE_MANAGER", "ROLE_ADMIN");
    }

    /**
     * Determines whether the authenticated principal can administer accounts.
     *
     * @param authentication the current authentication
     * @return {@code true} for an employee with the ADMIN authority; {@code false} otherwise
     */
    public boolean canAdministerAccount(Authentication authentication) {
        return isEmployee(authentication)
                && hasAuthority(authentication, "ROLE_ADMIN");
    }

    // READ ACCESS BY ACCOUNT TYPE

    /**
     * Determines whether the authenticated principal can access a checking account.
     *
     * @param accountId      the ID of the checking account
     * @param authentication the current authentication
     * @return {@code true} for any employee, or for a client that owns the account
     * and whose owner is not logically deleted; {@code false} otherwise
     */
    public boolean canAccessCheckingAccount(Long accountId, Authentication authentication) {
        if (accountId == null || !isAuthenticated(authentication)) {
            return false;
        }
        if (isEmployee(authentication)) {
            return canSearchAccount(authentication);
        }
        Long clientId = getClientId(authentication);
        return clientId != null
                && checkingAccountRepository.existsByIdAndOwnerIdAndOwnerDeletedFalse(accountId, clientId);
    }

    /**
     * Determines whether the authenticated principal can access a savings account.
     *
     * @param accountId      the ID of the savings account
     * @param authentication the current authentication
     * @return {@code true} for any employee, or for a client that owns the account
     * and whose owner is not logically deleted; {@code false} otherwise
     */
    public boolean canAccessSavingsAccount(Long accountId, Authentication authentication) {
        if (accountId == null || !isAuthenticated(authentication)) {
            return false;
        }
        if (isEmployee(authentication)) {
            return canSearchAccount(authentication);
        }
        Long clientId = getClientId(authentication);
        return clientId != null
                && savingsAccountRepository.existsByIdAndOwnerIdAndOwnerDeletedFalse(accountId, clientId);
    }

    /**
     * Determines whether the authenticated principal can access a joint account.
     *
     * @param accountId      the ID of the joint account
     * @param authentication the current authentication
     * @return {@code true} for any employee, or for a client that is one of the
     * account owners and is not logically deleted; {@code false} otherwise
     */
    public boolean canAccessJointAccount(Long accountId, Authentication authentication) {
        if (accountId == null || !isAuthenticated(authentication)) {
            return false;
        }
        if (isEmployee(authentication)) {
            return canSearchAccount(authentication);
        }
        Long clientId = getClientId(authentication);
        return clientId != null
                && jointAccountRepository.existsByAccountIdAndOwnerId(accountId, clientId);
    }

    // BUSINESS ACCOUNT ACCESS

    /**
     * Determines whether the authenticated principal can read a business account.
     *
     * @param accountId      the ID of the business account
     * @param authentication the current authentication
     * @return {@code true} for any employee, or for a client that is the company's
     * principal partner or an active member with the ADMIN, OPERATOR or VIEWER role;
     * {@code false} otherwise
     */
    public boolean canReadBusinessAccount(Long accountId, Authentication authentication) {
        return canAccessBusinessAccount(accountId, authentication,
                canSearchAccount(authentication),
                List.of(CompanyMemberRole.ADMIN, CompanyMemberRole.OPERATOR, CompanyMemberRole.VIEWER));
    }

    /**
     * Determines whether the authenticated principal can operate a business account.
     *
     * @param accountId      the ID of the business account
     * @param authentication the current authentication
     * @return {@code true} for any employee, or for a client that is the company's
     * principal partner or an active member with the ADMIN or OPERATOR role;
     * {@code false} otherwise
     */
    public boolean canOperateBusinessAccount(Long accountId, Authentication authentication) {
        return canAccessBusinessAccount(accountId, authentication,
                canSearchAccount(authentication),
                List.of(CompanyMemberRole.ADMIN, CompanyMemberRole.OPERATOR));
    }

    /**
     * Determines whether the authenticated principal can manage a business account.
     *
     * @param accountId      the ID of the business account
     * @param authentication the current authentication
     * @return {@code true} for a MANAGER or ADMIN employee, or for a client that is the
     * company's principal partner or an active member with the ADMIN role;
     * {@code false} otherwise
     */
    public boolean canManageBusinessAccount(Long accountId, Authentication authentication) {
        return canAccessBusinessAccount(accountId, authentication,
                canManageAccount(authentication),
                List.of(CompanyMemberRole.ADMIN));
    }

    /**
     * Determines whether the authenticated principal can administer a business account.
     *
     * @param accountId      the ID of the business account
     * @param authentication the current authentication
     * @return {@code true} for an ADMIN employee, or for a client that is the
     * company's principal partner or an active member with the ADMIN role;
     * {@code false} otherwise
     */
    public boolean canAdministerBusinessAccount(Long accountId, Authentication authentication) {
        return canAccessBusinessAccount(accountId, authentication,
                canAdministerAccount(authentication),
                List.of(CompanyMemberRole.ADMIN));
    }

    /**
     * Determines whether the authenticated principal can read a business account
     * by its account number.
     *
     * @param accountNumber  the account number of the business account
     * @param authentication the current authentication
     * @return {@code true} for any employee, or for a client that is the company's
     * principal partner or an active member with the ADMIN, OPERATOR or VIEWER role;
     * {@code false} otherwise
     */
    public boolean canReadBusinessAccountByNumber(String accountNumber, Authentication authentication) {
        if (accountNumber == null || accountNumber.isBlank() || !isAuthenticated(authentication)) {
            return false;
        }

        String normalizedNumber = normalizeBusinessAccountNumber(accountNumber);
        Optional<BusinessAccount> businessAccount =
                businessAccountRepository.findByAccountNumberAndDeletedFalse(normalizedNumber);

        return businessAccount.isPresent()
                && canReadBusinessAccount(businessAccount.get().getId(), authentication);
    }

    /**
     * Determines whether the authenticated principal can manage a business account
     * identified by its account number.
     *
     * @param accountNumber  the account number of the business account
     * @param authentication the current authentication
     * @return {@code true} if the principal can manage the account
     */
    public boolean canManageBusinessAccountByNumber(String accountNumber, Authentication authentication) {
        if (accountNumber == null || accountNumber.isBlank() || !isAuthenticated(authentication)) {
            return false;
        }

        String normalizedNumber = normalizeBusinessAccountNumber(accountNumber);
        Optional<BusinessAccount> businessAccount =
                businessAccountRepository.findByAccountNumberAndDeletedFalse(normalizedNumber);

        return businessAccount.isPresent()
                && canManageBusinessAccount(businessAccount.get().getId(), authentication);
    }

    /**
     * Determines whether the authenticated principal may list business accounts
     * for the specified client.
     *
     * @param clientId       the client ID being queried
     * @param authentication the current authentication
     * @return {@code true} for an authorized employee or for the client itself
     */
    public boolean canListBusinessAccountsForClient(Long clientId, Authentication authentication) {
        if (clientId == null || clientId <= 0 || !isAuthenticated(authentication)) {
            return false;
        }
        if (isEmployee(authentication)) {
            return canSearchAccount(authentication);
        }

        Long authenticatedClientId = getClientId(authentication);
        return authenticatedClientId != null && authenticatedClientId.equals(clientId);
    }

    // ACCOUNT OPENING

    /**
     * Determines whether the authenticated principal can open a checking or savings
     * account for the given owner.
     *
     * @param ownerId        the ID of the client who will own the account
     * @param authentication the current authentication
     * @return {@code true} for a MANAGER or ADMIN employee, or for a client opening
     * an account in their own name; {@code false} otherwise
     */
    public boolean canOpenAccountFor(Long ownerId, Authentication authentication) {
        if (ownerId == null || !isAuthenticated(authentication)) {
            return false;
        }
        if (isEmployee(authentication)) {
            return canManageAccount(authentication);
        }
        return ownerId.equals(getClientId(authentication));
    }

    /**
     * Determines whether the authenticated principal can open a joint account
     * for the given owners.
     *
     * @param ownerIds       the IDs of the clients who will own the account
     * @param authentication the current authentication
     * @return {@code true} for a MANAGER or ADMIN employee, or for a client opening
     * an account in their own name; {@code false} otherwise
     */
    public boolean canOpenJointAccountFor(Collection<Long> ownerIds, Authentication authentication) {
        if (ownerIds == null || ownerIds.isEmpty() || !isAuthenticated(authentication)) {
            return false;
        }
        if (isEmployee(authentication)) {
            return canManageAccount(authentication);
        }

        Long clientId = getClientId(authentication);
        return clientId != null && ownerIds.contains(clientId);
    }

    /**
     * Determines whether the authenticated principal can open a business account
     * for the given company.
     *
     * <p>Delegates to {@link CompanyAuthorization#canManageMembers(Long, Authentication)},
     * which allows MANAGER and ADMIN employees, the company's principal partner,
     * and active members with the ADMIN role.</p>
     *
     * @param companyId      the ID of the company that will own the account
     * @param authentication the current authentication
     * @return {@code true} if the principal may act as manager of the company;
     * {@code false} otherwise
     */
    public boolean canOpenBusinessAccountFor(Long companyId, Authentication authentication) {
        return companyAuthorization.canManageMembers(companyId, authentication);
    }

    // PRIVATE HELPERS

    /**
     * Shared rule for business account access: an employee passes by role, and a
     * client passes if the account is accessible to them through the company.
     *
     * @param accountId       the ID of the business account
     * @param authentication  the current authentication
     * @param employeeAllowed whether the employee role is enough for this operation
     * @param roles           the company member roles accepted for a client
     * @return {@code true} if access is granted; {@code false} otherwise
     */
    private boolean canAccessBusinessAccount(Long accountId, Authentication authentication,
                                             boolean employeeAllowed, List<CompanyMemberRole> roles) {
        if (accountId == null || !isAuthenticated(authentication)) {
            return false;
        }
        if (isEmployee(authentication)) {
            return employeeAllowed;
        }
        Long clientId = getClientId(authentication);
        return clientId != null
                && businessAccountRepository.existsAccessibleByClient(
                accountId, clientId, MembershipStatus.ACTIVE, roles);
    }

    /**
     * Gets the client ID from the token, only when the principal has CLIENT identity.
     *
     * @param authentication the current authentication
     * @return the client ID, or {@code null} if the principal is not a client
     * or the ID is missing or invalid
     */
    private Long getClientId(Authentication authentication) {
        JwtAuthenticationToken jwt = (JwtAuthenticationToken) authentication;
        if (!UserIdentityType.CLIENT.name().equals(jwt.getToken().getClaimAsString("identity_type"))) {
            return null;
        }
        return getUserId(jwt);
    }

    /**
     * Gets the user ID from the {@code user_id} claim.
     *
     * @param authentication the JWT authentication token
     * @return the user ID, or {@code null} if it is missing or invalid
     */
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

    /**
     * Determines whether the principal is authenticated with a JWT.
     *
     * @param authentication the current authentication
     * @return {@code true} if authenticated with a JWT; {@code false} otherwise
     */
    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication instanceof JwtAuthenticationToken;
    }

    /**
     * Normalizes a business account number from eight digits or its full BA format.
     */
    private String normalizeBusinessAccountNumber(String accountNumber) {
        String trimmedNumber = accountNumber.trim();
        if (trimmedNumber.matches("\\d{8}")) {
            return "BA-" + trimmedNumber;
        }
        if (!trimmedNumber.matches("BA-\\d{8}")) {
            return "";
        }
        return trimmedNumber;
    }

    /**
     * Determines whether the principal is an employee with a user ID.
     *
     * @param authentication the current authentication
     * @return {@code true} for an authenticated employee JWT with a user ID;
     * {@code false} otherwise
     */
    private boolean isEmployee(Authentication authentication) {
        return isAuthenticated(authentication)
                && UserIdentityType.EMPLOYEE.name().equals(
                ((JwtAuthenticationToken) authentication).getToken().getClaimAsString("identity_type"))
                && getUserId((JwtAuthenticationToken) authentication) != null;
    }

    /**
     * Determines whether the principal has the specified authority.
     *
     * @param authentication the current authentication
     * @param authority      the authority to look for
     * @return {@code true} if the principal has the authority; {@code false} otherwise
     */
    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> authority.equals(granted.getAuthority()));
    }

    /**
     * Determines whether the principal has at least one of the specified authorities.
     *
     * @param authentication the current authentication
     * @param authorities    the authorities to look for
     * @return {@code true} if the principal has any of them; {@code false} otherwise
     */
    private boolean hasAnyAuthority(Authentication authentication, String... authorities) {
        return Arrays.stream(authorities)
                .anyMatch(authority -> hasAuthority(authentication, authority));
    }
}