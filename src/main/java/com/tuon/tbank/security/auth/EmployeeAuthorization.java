package com.tuon.tbank.security.auth;

import com.tuon.tbank.enums.auth.UserIdentityType;
import com.tuon.tbank.enums.person.EmployeeRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Checks employee identity and role authorities for client-related operations.
 */
@Component("employeeAuthorization")
public class EmployeeAuthorization {

    /**
     * Determines whether the authenticated principal is an employee with any
     * authority defined by {@link EmployeeRole}.
     *
     * @param authentication the current authentication
     * @return {@code true} for an authenticated employee JWT with a user ID and a recognized employee role;
     *         {@code false} otherwise
     */
    public boolean canSearchClients(Authentication authentication) {
        return hasEmployeeIdentity(authentication)
                && Arrays.stream(EmployeeRole.values())
                .anyMatch(role -> hasAuthority(authentication, "ROLE_" + role.name()));
    }

    /**
     * Determines whether the authenticated principal is an employee with the ADMIN authority.
     *
     * @param authentication the current authentication
     * @return {@code true} for an authenticated employee JWT with a user ID and the ADMIN authority;
     *         {@code false} otherwise
     */
    public boolean canAdministerClients(Authentication authentication) {
        return hasEmployeeIdentity(authentication)
                && hasAuthority(authentication, "ROLE_" + EmployeeRole.ADMIN.name());
    }

    /**
     * Determines whether the authenticated principal is an employee with the MANAGER or ADMIN authority.
     *
     * @param authentication the current authentication
     * @return {@code true} for an authenticated employee JWT with a user ID and either authority;
     *         {@code false} otherwise
     */
    public boolean canManageClients(Authentication authentication) {
        return hasEmployeeIdentity(authentication)
                && (hasAuthority(authentication, "ROLE_" + EmployeeRole.MANAGER.name())
                || hasAuthority(authentication, "ROLE_" + EmployeeRole.ADMIN.name()));
    }

    /**
     * Determines whether the authenticated principal is an employee with any
     * authority defined by {@link EmployeeRole} for company-related operations.
     *
     * @param authentication the current authentication
     * @return {@code true} for an authenticated employee JWT with a user ID and a recognized employee role;
     *         {@code false} otherwise
     */
    public boolean canSearchCompanies(Authentication authentication) {
        return hasEmployeeIdentity(authentication)
                && Arrays.stream(EmployeeRole.values())
                .anyMatch(role -> hasAuthority(authentication, "ROLE_" + role.name()));
    }

    /**
     * Determines whether the authenticated principal is an employee with the ADMIN authority for company-related operations.
     *
     * @param authentication the current authentication
     * @return {@code true} for an authenticated employee JWT with a user ID and the ADMIN authority;
     *         {@code false} otherwise
     */
    public boolean canAdministerCompany(Authentication authentication) {
        return hasEmployeeIdentity(authentication)
                && hasAuthority(authentication, "ROLE_" + EmployeeRole.ADMIN.name());
    }

    /**
     * Determines whether the authenticated principal is an employee with the MANAGER or ADMIN authority for company-related operations.
     *
     * @param authentication the current authentication
     * @return {@code true} for an authenticated employee JWT with a user ID and either authority;
     *         {@code false} otherwise
     */
    public boolean canManageCompany(Authentication authentication) {
        return hasEmployeeIdentity(authentication)
                && (hasAuthority(authentication, "ROLE_" + EmployeeRole.MANAGER.name())
                || hasAuthority(authentication, "ROLE_" + EmployeeRole.ADMIN.name()));
    }

    /**
     * Determines whether the authenticated principal is an employee with any
     * authority defined by {@link EmployeeRole} for employee-related operations.
     *
     * @param authentication the current authentication
     * @return {@code true} for an authenticated employee JWT with a user ID and a recognized employee role;
     *         {@code false} otherwise
     */
    public boolean canSearchEmployees(Authentication authentication) {
        return hasEmployeeIdentity(authentication)
                && Arrays.stream(EmployeeRole.values())
                .anyMatch(role -> hasAuthority(authentication, "ROLE_" + role.name()));
    }

    /**
     * Determines whether the authenticated principal is an employee with the ADMIN authority for employee-related operations.
     *
     * @param authentication the current authentication
     * @return {@code true} for an authenticated employee JWT with a user ID and the ADMIN authority;
     *         {@code false} otherwise
     */
    public boolean canAdministerEmployees(Authentication authentication) {
        return hasEmployeeIdentity(authentication)
                && hasAuthority(authentication, "ROLE_" + EmployeeRole.ADMIN.name());
    }

    /**
     * Determines whether the authenticated principal is an employee with the MANAGER or ADMIN authority for employee-related operations.
     *
     * @param authentication the current authentication
     * @return {@code true} for an authenticated employee JWT with a user ID and either authority;
     *         {@code false} otherwise
     */
    public boolean canManageEmployees(Authentication authentication) {
        return hasEmployeeIdentity(authentication)
                && (hasAuthority(authentication, "ROLE_" + EmployeeRole.MANAGER.name())
                || hasAuthority(authentication, "ROLE_" + EmployeeRole.ADMIN.name()));
    }

    public boolean isOwner(Long employeeId, Authentication authentication) {
        if (employeeId == null || authentication == null || !authentication.isAuthenticated()
                || !(authentication instanceof JwtAuthenticationToken jwt)) {
            return false;
        }
        Object identityType = jwt.getToken().getClaim("identity_type");
        Object userId = getUserId(jwt);
        return UserIdentityType.EMPLOYEE.name().equals(identityType)
                && hasEmployeeRole(authentication)
                && employeeId.equals(userId);
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

    /**
     * Determines whether the authenticated principal is an employee with any
     * authority defined by {@link EmployeeRole} for transaction reversal operations.
     *
     * @param authentication the current authentication
     * @return {@code true} for an authenticated employee JWT with a user ID and a recognized employee role;
     *         {@code false} otherwise
     */
    private boolean hasEmployeeIdentity(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication instanceof JwtAuthenticationToken jwt)) {
            return false;
        }
        return UserIdentityType.EMPLOYEE.name().equals(jwt.getToken().getClaim("identity_type"))
                && jwt.getToken().getClaim("user_id") != null;
    }

    /**
     * Checks if the authenticated principal has the specified authority.
     *
     * @param authentication the current authentication
     * @param requiredAuthority the authority to check for
     * @return {@code true} if the principal has the authority; {@code false} otherwise
     */
    private boolean hasAuthority(Authentication authentication, String requiredAuthority) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> requiredAuthority.equals(authority.getAuthority()));
    }

    private boolean hasEmployeeRole(Authentication authentication) {
        return Arrays.stream(EmployeeRole.values())
                .anyMatch(role -> hasAuthority(authentication, "ROLE_" + role.name()));
    }

    public boolean canReadEmployee(Long employeeId, Authentication authentication) {
        if (employeeId == null || authentication == null || !authentication.isAuthenticated()
                || !(authentication instanceof JwtAuthenticationToken jwt)) {
            return false;
        }
        Object identityType = jwt.getToken().getClaim("identity_type");
        if (UserIdentityType.EMPLOYEE.name().equals(identityType)) {
            return isOwner(employeeId, authentication)
                    || hasAuthority(authentication, "ROLE_ADMIN")
                    || hasAuthority(authentication, "ROLE_MANAGER");
        }
        return isOwner(employeeId, authentication);
    }
}
