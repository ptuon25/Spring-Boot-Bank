package com.tuon.tbank.security;

import com.tuon.tbank.security.auth.EmployeeAuthorization;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class EmployeeAuthorizationTests {

    private final EmployeeAuthorization employeeAuthorization = new EmployeeAuthorization();

    //IS OWNER TESTS

    @Test
    void isOwnerAcceptsEmployeeWithMatchingId() {
        Authentication auth = authentication("EMPLOYEE", 5L, "ROLE_ASSOCIATE");

        assertTrue(employeeAuthorization.isOwner(5L, auth));
    }

    @Test
    void isOwnerComparesUserIdAsText() {
        Authentication withLong = authentication("EMPLOYEE", 5L, "ROLE_ASSOCIATE");
        Authentication withInt = authentication("EMPLOYEE", 5, "ROLE_ASSOCIATE");
        Authentication withString = authentication("EMPLOYEE", "5", "ROLE_ASSOCIATE");

        assertTrue(employeeAuthorization.isOwner(5L, withLong));
        assertTrue(employeeAuthorization.isOwner(5L, withInt));
        assertTrue(employeeAuthorization.isOwner(5L, withString));
    }

    @Test
    void isOwnerRejectsDifferentId() {
        Authentication auth = authentication("EMPLOYEE", 5L, "ROLE_ASSOCIATE");

        assertTrue(!employeeAuthorization.isOwner(7L, auth));
    }

    @Test
    void isOwnerRejectsNullEmployeeId() {
        Authentication auth = authentication("EMPLOYEE", 5L, "ROLE_ASSOCIATE");

        assertFalse(employeeAuthorization.isOwner(null, auth));
    }

    @Test
    void isOwnerRejectsNullAuthentication() {
        assertFalse(employeeAuthorization.isOwner(5L, null));
    }

    @Test
    void isOwnerRejectsNonJwtAuthentication() {
        Authentication auth = UsernamePasswordAuthenticationToken
                .authenticated("5", "password", Arrays.asList(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));

        assertFalse(employeeAuthorization.isOwner(5L, auth));
    }

    @Test
    void isOwnerRejectsNonEmployeeAuthentication() {
        Authentication auth = authentication("CLIENT", 5L, "ROLE_ASSOCIATE");

        assertFalse(employeeAuthorization.isOwner(5L, auth));
    }

    @Test
    void isOwnerRejectsTokenWithoutUserId() {
        Authentication auth = authentication("EMPLOYEE", null, "ROLE_ASSOCIATE");

        assertFalse(employeeAuthorization.isOwner(5L, auth));
    }

    @Test
    void isOwnerRejectsClientIdentity() {
        Authentication auth = authentication("CLIENT", 5L, "ROLE_ASSOCIATE");

        assertFalse(employeeAuthorization.isOwner(5L, auth));
    }

    @Test
    void isOwnerRejectsEmployeeWithoutRole() {
        Authentication auth = authentication("EMPLOYEE", 5L);

        assertFalse(employeeAuthorization.isOwner(5L, auth));
    }

    @Test
    void isOwnerRejectsEmployeeWithWrongRole() {
        Authentication auth = authentication("EMPLOYEE", 5L, "ROLE_CLIENT");

        assertFalse(employeeAuthorization.isOwner(5L, auth));
    }

    // CAN READ TESTS

    @Test
    void canReadEmployeeAllowsOwner() {
        Authentication auth = authentication("EMPLOYEE", 5L, "ROLE_ASSOCIATE");

        assertTrue(employeeAuthorization.canReadEmployee(5L, auth));
    }

    @Test
    void canReadEmployeeRejectsNonOwner() {
        Authentication auth = authentication("EMPLOYEE", 5L, "ROLE_ASSOCIATE");

        assertFalse(employeeAuthorization.canReadEmployee(7L, auth));
    }

    @Test
    void canReadEmployeeRejectsNonEmployee() {
        Authentication auth = authentication("CLIENT", 5L, "ROLE_CLIENT");

        assertFalse(employeeAuthorization.canReadEmployee(5L, auth));
    }

    @Test
    void canReadEmployeeRejectsEmployeeWithoutRole() {
        Authentication auth = authentication("EMPLOYEE", 5L);

        assertFalse(employeeAuthorization.canReadEmployee(5L, auth));
    }

    @Test
    void canReadEmployeeRejectsEmployeeWithWrongRole() {
        Authentication auth = authentication("EMPLOYEE", 5L, "ROLE_CLIENT");

        assertFalse(employeeAuthorization.canReadEmployee(5L, auth));
    }

    @Test
    void canReadEmployeeRejectsNullAuthentication() {
        assertFalse(employeeAuthorization.canReadEmployee(5L, null));
    }

    @Test
    void canReadEmployeeRejectsNullEmployeeId() {
        Authentication auth = authentication("EMPLOYEE", 5L, "ROLE_EMPLOYEE");

        assertFalse(employeeAuthorization.canReadEmployee(null, auth));
    }

    @Test
    void canReadEmployeeRejectsNonJwtAuthentication() {
        Authentication auth = UsernamePasswordAuthenticationToken
                .authenticated("5", "password", Arrays.asList(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));

        assertFalse(employeeAuthorization.canReadEmployee(5L, auth));
    }

    @Test
    void canReadEmployeeRejectsTokenWithoutUserId() {
        Authentication auth = authentication("EMPLOYEE", null, "ROLE_ASSOCIATE");

        assertFalse(employeeAuthorization.canReadEmployee(5L, auth));
    }

    @Test
    void canReadEmployeeRejectsClientIdentity() {
        Authentication auth = authentication("CLIENT", 5L, "ROLE_ASSOCIATE");

        assertFalse(employeeAuthorization.canReadEmployee(5L, auth));
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
