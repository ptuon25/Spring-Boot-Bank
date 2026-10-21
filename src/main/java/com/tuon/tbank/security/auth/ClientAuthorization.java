package com.tuon.tbank.security.auth;

import com.tuon.tbank.enums.auth.UserIdentityType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("clientAuthorization")
public class ClientAuthorization {

    public boolean canReadClient(Long clientId, Authentication authentication) {
        if (clientId == null || authentication == null || !authentication.isAuthenticated()
                || !(authentication instanceof JwtAuthenticationToken jwt)) {
            return false;
        }
        Object identityType = jwt.getToken().getClaim("identity_type");
        Object userId = jwt.getToken().getClaim("user_id");
        if (UserIdentityType.EMPLOYEE.name().equals(identityType)) {
            return hasAuthority(authentication, "ROLE_ADMIN") || hasAuthority(authentication, "ROLE_MANAGER");
        }
        return UserIdentityType.CLIENT.name().equals(identityType)
                && hasAuthority(authentication, "ROLE_CLIENT")
                && userId != null
                && clientId.toString().equals(userId.toString());
    }

    private boolean hasAuthority(Authentication authentication, String requiredAuthority) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> requiredAuthority.equals(authority.getAuthority()));
    }
}
