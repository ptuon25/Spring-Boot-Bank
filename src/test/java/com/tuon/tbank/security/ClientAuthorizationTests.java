package com.tuon.tbank.security;

import com.tuon.tbank.security.auth.ClientAuthorization;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientAuthorizationTests {

    private final ClientAuthorization clientAuthorization = new ClientAuthorization();

    // IS OWNER TEST

    @Test
    void isOwnerAcceptsClientWithMatchingId() {
        Authentication auth = authentication("CLIENT", 5L, "ROLE_CLIENT");

        assertTrue(clientAuthorization.isOwner(5L, auth));
    }

    @Test
    void isOwnerComparesUserIdAsText() {
        Authentication withLong = authentication("CLIENT", 5L, "ROLE_CLIENT");
        Authentication withInt = authentication("CLIENT", 5, "ROLE_CLIENT");
        Authentication withString = authentication("CLIENT", "5", "ROLE_CLIENT");

        assertTrue(clientAuthorization.isOwner(5L, withLong));
        assertTrue(clientAuthorization.isOwner(5L, withInt));
        assertTrue(clientAuthorization.isOwner(5L, withString));
    }

    @Test
    void isOwnerRejectsDifferentId() {
        Authentication auth = authentication("CLIENT", 5L, "ROLE_CLIENT");

        assertFalse(clientAuthorization.isOwner(7L, auth));
    }

    @Test
    void isOwnerRejectsNullClientId() {
        Authentication auth = authentication("CLIENT", 5L, "ROLE_CLIENT");

        assertFalse(clientAuthorization.isOwner(null, auth));
    }

    @Test
    void isOwnerRejectsNullAuthentication() {
        assertFalse(clientAuthorization.isOwner(5L, null));
    }

    @Test
    void isOwnerRejectsNonJwtAuthentication() {
        Authentication auth = UsernamePasswordAuthenticationToken.authenticated(
                "5", "password", List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));

        assertFalse(clientAuthorization.isOwner(5L, auth));
    }

    @Test
    void isOwnerRejectsTokenWithoutUserId() {
        Authentication auth = authentication("CLIENT", null, "ROLE_CLIENT");

        assertFalse(clientAuthorization.isOwner(5L, auth));
    }

    @Test
    void isOwnerRejectsEmployeeIdentity() {
        Authentication auth = authentication("EMPLOYEE", 5L, "ROLE_CLIENT");

        assertFalse(clientAuthorization.isOwner(5L, auth));
    }

    @Test
    void isOwnerRejectsClientWithoutClientAuthority() {
        Authentication auth = authentication("CLIENT", 5L, "ROLE_ADMIN");

        assertFalse(clientAuthorization.isOwner(5L, auth));
    }

    // CAN READ CLIENT TEST

    @Test
    void canReadClientAllowsOwner() {
        Authentication auth = authentication("CLIENT", 5L, "ROLE_CLIENT");

        assertTrue(clientAuthorization.canReadClient(5L, auth));
    }

    @Test
    void canReadClientRejectsOtherClient() {
        Authentication auth = authentication("CLIENT", 5L, "ROLE_CLIENT");

        assertFalse(clientAuthorization.canReadClient(7L, auth));
    }

    @Test
    void canReadClientAllowsManager() {
        Authentication auth = authentication("EMPLOYEE", 2L, "ROLE_MANAGER");

        assertTrue(clientAuthorization.canReadClient(5L, auth));
    }

    @Test
    void canReadClientAllowsAdmin() {
        Authentication auth = authentication("EMPLOYEE", 3L, "ROLE_ADMIN");

        assertTrue(clientAuthorization.canReadClient(5L, auth));
    }

    @Test
    void canReadClientRejectsAssociate() {
        Authentication auth = authentication("EMPLOYEE", 1L, "ROLE_ASSOCIATE");

        assertFalse(clientAuthorization.canReadClient(5L, auth));
    }

    @Test
    void canReadClientRejectsClientWithAdminAuthority() {
        Authentication auth = authentication("CLIENT", 7L, "ROLE_ADMIN");

        assertFalse(clientAuthorization.canReadClient(7L, auth));
    }

    @Test
    void canReadClientRejectsNullInputs() {
        Authentication auth = authentication("CLIENT", 5L, "ROLE_CLIENT");

        assertFalse(clientAuthorization.canReadClient(null, auth));
        assertFalse(clientAuthorization.canReadClient(5L, null));
        assertFalse(clientAuthorization.canReadClient(null, null));
    }

    // AUXILIARY

    private Authentication authentication(String identityType, Object userId, String... authorities) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("identity_type", identityType);
        if (userId != null) {
            builder.claim("user_id", userId);
        }
        return new JwtAuthenticationToken(builder.build(),
                Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList());
    }

}