package com.tuon.tbank.security.auth;

import com.tuon.tbank.enums.auth.UserIdentityType;
import com.tuon.tbank.enums.person.EmployeeRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component("employeeAuthorization")
public class EmployeeAuthorization {

    public boolean canSearchClients(Authentication authentication) {
        return hasEmployeeIdentity(authentication)
                && Arrays.stream(EmployeeRole.values())
                .anyMatch(role -> hasAuthority(authentication, "ROLE_" + role.name()));
    }

    public boolean canAdministerClients(Authentication authentication) {
        return hasEmployeeIdentity(authentication)
                && hasAuthority(authentication, "ROLE_" + EmployeeRole.ADMIN.name());
    }

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
