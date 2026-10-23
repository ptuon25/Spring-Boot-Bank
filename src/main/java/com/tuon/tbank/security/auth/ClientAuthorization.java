package com.tuon.tbank.security.auth;

import com.tuon.tbank.enums.auth.UserIdentityType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Provides authorization checks for client-related operations based on
 * the authenticated user's identity and granted authorities.
 */
@Component("clientAuthorization")
public class ClientAuthorization {

    /**
     * Determines whether the authenticated user is allowed to read a client's
     * information. Employees with ADMIN or MANAGER roles may access client
     * information, while clients may only access their own information.
     *
     * @param clientId the unique identifier of the client to access
     * @param authentication the current authentication information
     * @return true if the user is authorized to read the client's information,
     *         false otherwise
     */
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
        return isOwner(clientId, authentication);
    }

    /**
     * Checks whether the authenticated user has the specified authority.
     *
     * @param authentication the current authentication information
     * @param requiredAuthority the authority required for access
     * @return true if the user has the required authority, false otherwise
     */
    private boolean hasAuthority(Authentication authentication, String requiredAuthority) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> requiredAuthority.equals(authority.getAuthority()));
    }

    /**
     * Determines whether the authenticated user is the client identified
     * by the specified client ID.
     *
     * @param clientId the unique identifier of the client
     * @param authentication the current authentication information
     * @return true if the authenticated user is the specified client,
     *         false otherwise
     */
    public boolean isOwner(Long clientId, Authentication authentication) {
        if (clientId == null || authentication == null || !authentication.isAuthenticated()
                || !(authentication instanceof JwtAuthenticationToken jwt)) {
            return false;
        }
        Object identityType = jwt.getToken().getClaim("identity_type");
        Object userId = jwt.getToken().getClaim("user_id");
        return UserIdentityType.CLIENT.name().equals(identityType)
                && hasAuthority(authentication, "ROLE_CLIENT")
                && userId != null
                && clientId.toString().equals(userId.toString());
    }
}
