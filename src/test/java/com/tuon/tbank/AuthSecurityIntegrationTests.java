package com.tuon.tbank;

import com.tuon.tbank.repository.person.ClientRepository;
import com.tuon.tbank.enums.person.ClientStatus;
import com.tuon.tbank.service.person.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientService clientService;

    @Test
    void clientCanLogInAndReadOnlyOwnProfile() throws Exception {
        String token = login("john_doe@hotmail.com", "client-test-password");
        Long ownId = clientRepository.findByEmail("john_doe@hotmail.com").orElseThrow().getId();
        Long otherId = clientRepository.findByEmail("jane_smith@gmail.com").orElseThrow().getId();

        mockMvc.perform(get("/clients/{id}", ownId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.checkingAccounts").doesNotExist());
        mockMvc.perform(get("/clients/{id}", otherId).header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanSearchClientsBySupportedCriteria() throws Exception {
        String token = login("alice_johnson@tbank.com", "manager-test-password");

        mockMvc.perform(get("/clients").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/clients/search/name").param("name", "John Doe")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"));
        mockMvc.perform(get("/clients/search/status").param("status", "ACTIVE")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/clients/search/email").param("email", "john_doe@hotmail.com")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john_doe@hotmail.com"));
        mockMvc.perform(get("/clients/search/document").param("document", "1234567890")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void clientCannotSearchOtherClientsOrUseSearchFilters() throws Exception {
        String token = login("john_doe@hotmail.com", "client-test-password");

        mockMvc.perform(get("/clients").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/clients/search/name").param("name", "Jane Smith")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/clients/search/status").param("status", "ACTIVE")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/clients/search/email").param("email", "jane_smith@gmail.com")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/clients/search/document").param("document", "0987654321")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeeIdentityIsRequiredEvenWhenTokenHasAdminAuthority() throws Exception {
        String clientIdentityWithAdminAuthority = createToken("CLIENT", "ROLE_ADMIN");

        mockMvc.perform(get("/clients").header("Authorization", bearer(clientIdentityWithAdminAuthority)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/clients/1").header("Authorization", bearer(clientIdentityWithAdminAuthority)))
                .andExpect(status().isForbidden());
    }

    @Test
    void associateCanSearchButCannotModifyOrDeleteClients() throws Exception {
        String token = login("bob_williams@tbank.com", "associate-test-password");

        mockMvc.perform(get("/clients/search/name").param("name", "John Doe")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(put("/clients/1")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/clients/1")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void clientCannotInvokeClientStatusOperationsDirectly() {
        Jwt clientToken = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("identity_type", "CLIENT")
                .claim("user_id", 1)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(clientToken, List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))));

        try {
            assertThrows(AccessDeniedException.class, () -> clientService.blockClient(1L));
            assertThrows(AccessDeniedException.class, () -> clientService.deactivateClient(1L));
            assertThrows(AccessDeniedException.class, () -> clientService.reactivateClient(1L));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @Transactional
    void employeeAdminCanInvokeClientStatusOperations() {
        Jwt adminToken = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("identity_type", "EMPLOYEE")
                .claim("user_id", 1)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(adminToken, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        Long clientId = clientRepository.findByEmail("john_doe@hotmail.com").orElseThrow().getId();

        try {
            assertEquals(ClientStatus.BLOCKED, clientService.blockClient(clientId).getStatus());
            assertEquals(ClientStatus.ACTIVE, clientService.reactivateClient(clientId).getStatus());
            assertEquals(ClientStatus.INACTIVE, clientService.deactivateClient(clientId).getStatus());
            assertEquals(ClientStatus.ACTIVE, clientService.reactivateClient(clientId).getStatus());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void employeeCanLogInAndReceivesExpectedAuthority() throws Exception {
        String token = login("alice_johnson@tbank.com", "manager-test-password");
        mockMvc.perform(get("/market-ratings").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanReachAdministrativeEndpointWithoutPasswordHashInResponse() throws Exception {
        String token = login("admin@tbank.test", "admin-test-password");
        MvcResult result = mockMvc.perform(get("/employees").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(result.getResponse().getContentAsString().contains("passwordHash"));
    }

    @Test
    void incorrectPasswordAndUnknownUserReturnSameUnauthorizedResponse() throws Exception {
        String badPassword = loginFailure("john_doe@hotmail.com", "wrong-password");
        String unknownUser = loginFailure("missing@tbank.test", "wrong-password");

        assertEquals(badPassword, unknownUser);
        assertTrue(badPassword.contains("Invalid email or password"));
    }

    @Test
    void blockedAndNonActiveUsersCannotLogIn() throws Exception {
        loginFailure("blocked_client@tbank.com", "blocked-test-password");
        loginFailure("on_leave@tbank.test", "leave-test-password");
    }

    @Test
    void protectedEndpointsRejectMissingCredentials() throws Exception {
        mockMvc.perform(get("/transfers"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void managerCannotAccessAdminOnlyEndpoint() throws Exception {
        String token = login("alice_johnson@tbank.com", "manager-test-password");
        mockMvc.perform(get("/employees").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void expiredAndMalformedTokensAreRejected() throws Exception {
        String expiredToken = createExpiredAdminToken();
        mockMvc.perform(get("/employees").header("Authorization", bearer(expiredToken)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/employees").header("Authorization", bearer("malformed.token.value")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationHashesPasswordAndDoesNotReturnSensitiveFields() throws Exception {
        String password = "new-client-test-password";
        String registration = """
                {
                  "name": "New Client",
                  "birthDate": "1990-01-01",
                  "document": "9911223344",
                  "email": "New.Client@tbank.test",
                  "password": "%s"
                }
                """.formatted(password);
        MvcResult result = mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertFalse(body.contains(password));
        assertFalse(body.contains("passwordHash"));
        String storedHash = clientRepository.findByEmail("new.client@tbank.test").orElseThrow().getPasswordHash();
        assertNotEquals(password, storedHash);
        assertTrue(passwordEncoder.matches(password, storedHash));
    }

    @Test
    void loginResponseContainsTokenMetadataButNoSensitiveData() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@tbank.test","password":"admin-test-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andReturn();

        assertTrue(result.getResponse().getContentAsString().contains("\"accessToken\":\""));
        assertFalse(result.getResponse().getContentAsString().contains("passwordHash"));
    }

    @Test
    void publicRegistrationValidatesPasswordAndLoginInput() throws Exception {
        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Invalid","birthDate":"1990-01-01","document":"1122334455",
                                 "email":"invalid@tbank.test","password":"short"}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-an-email","password":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return extractAccessToken(result.getResponse().getContentAsString());
    }

    private String loginFailure(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isUnauthorized())
                .andReturn();
        return result.getResponse().getContentAsString();
    }

    private String createExpiredAdminToken() {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("tbank-test")
                .issuedAt(now.minusSeconds(120))
                .expiresAt(now.minusSeconds(60))
                .subject("employee:1")
                .claim("identity_type", "EMPLOYEE")
                .claim("user_id", 1)
                .claim("authorities", List.of("ROLE_ADMIN"))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private String createToken(String identityType, String authority) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("tbank-test")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .subject(identityType.toLowerCase() + ":1")
                .claim("identity_type", identityType)
                .claim("user_id", 1)
                .claim("authorities", List.of(authority))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String extractAccessToken(String body) {
        Matcher matcher = Pattern.compile("\"accessToken\":\"([^\"]+)\"").matcher(body);
        if (!matcher.find()) {
            throw new AssertionError("Login response did not include an access token");
        }
        return matcher.group(1);
    }
}
