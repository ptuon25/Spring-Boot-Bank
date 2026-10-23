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

    private boolean hasEmployeeIdentity(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication instanceof JwtAuthenticationToken jwt)) {
            return false;
        }
        return UserIdentityType.EMPLOYEE.name().equals(jwt.getToken().getClaim("identity_type"))
                && jwt.getToken().getClaim("user_id") != null;
    }

    private boolean hasAuthority(Authentication authentication, String requiredAuthority) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> requiredAuthority.equals(authority.getAuthority()));
    }
}
