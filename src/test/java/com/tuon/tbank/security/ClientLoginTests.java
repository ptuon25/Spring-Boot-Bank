package com.tuon.tbank.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientLoginTests {

    private static final AtomicLong COUNTER = new AtomicLong(7000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtDecoder jwtDecoder;

    // LOGIN SUCCESS TEST

    @Test
    void activeClientCanLogin() throws Exception {
        TestClient client = createClient();

        attemptLogin(client.email(), client.password())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void loginTokenContainsClientIdentity() throws Exception {
        TestClient client = createClient();

        Jwt jwt = jwtDecoder.decode(login(client.email(), client.password()));

        assertEquals("CLIENT", jwt.getClaim("identity_type"));
        assertEquals(client.id().toString(), jwt.getClaim("user_id").toString());
        assertEquals(List.of("ROLE_CLIENT"), jwt.getClaimAsStringList("authorities"));
    }

    @Test
    void reactivatedClientCanLoginAgain() throws Exception {
        TestClient client = createClient();
        blockClient(client.id());
        reactivateClient(client.id());

        attemptLogin(client.email(), client.password())
                .andExpect(status().isOk());
    }

    // LOGIN REJECTION TEST

    @Test
    void clientBlockedAfterRegistrationCannotLogin() throws Exception {
        TestClient client = createClient();
        blockClient(client.id());

        attemptLogin(client.email(), client.password())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void inactiveClientCannotLogin() throws Exception {
        TestClient client = createClient();
        deactivateClient(client.id());

        attemptLogin(client.email(), client.password())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deletedClientCannotLogin() throws Exception {
        TestClient client = createClient();
        deleteClient(client.id());

        attemptLogin(client.email(), client.password())
                .andExpect(status().isUnauthorized());
    }

    // EMAIL FORMAT TEST (documents current behavior)

    @Test
    void loginRejectsSpacesAroundEmail() throws Exception {
        TestClient client = createClient();

        attemptLogin("  " + client.email() + "  ", client.password())
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithUppercaseEmailBehavior() throws Exception {
        TestClient client = createClient();

        attemptLogin(client.email().toUpperCase(Locale.ROOT), client.password())
                .andExpect(status().isUnauthorized());
    }

    // KNOWN LIMIT TEST (documents current behavior)

    @Test
    void oldTokenStillWorksAfterPasswordChange() throws Exception {
        TestClient client = createClient();
        String oldToken = login(client.email(), client.password());

        mockMvc.perform(patch("/clients/{id}/password", client.id())
                        .header("Authorization", bearer(oldToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"%s","newPassword":"new-password-1234"}
                                """.formatted(client.password())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/clients/{id}", client.id())
                        .header("Authorization", bearer(oldToken)))
                .andExpect(status().isOk());
    }

    @Test
    void oldTokenStillWorksAfterBlock() throws Exception {
        TestClient client = createClient();
        String oldToken = login(client.email(), client.password());
        blockClient(client.id());

        mockMvc.perform(get("/clients/{id}", client.id())
                        .header("Authorization", bearer(oldToken)))
                .andExpect(status().isOk());
    }

    // AUXILIARY

    private record TestClient(Long id, String email, String password) {
    }

    private static String uniqueDocument() {
        return "4000" + COUNTER.incrementAndGet();
    }

    private static String uniqueEmail() {
        return "login" + COUNTER.incrementAndGet() + "@tbank.test";
    }

    private TestClient createClient() throws Exception {
        String email = uniqueEmail();
        String password = "a-strong-password-1";
        MvcResult result = mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Login Client","birthDate":"1990-01-01","document":"%s","email":"%s","password":"%s"}
                                """.formatted(uniqueDocument(), email, password)))
                .andExpect(status().isCreated())
                .andReturn();
        return new TestClient(extractId(result.getResponse().getContentAsString()), email, password);
    }

    // Faz o login SEM conferir o status: cada teste decide o que espera
    private ResultActions attemptLogin(String email, String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password)));
    }

    // Faz o login exigindo 200 e devolve o token
    private String login(String email, String password) throws Exception {
        MvcResult result = attemptLogin(email, password)
                .andExpect(status().isOk())
                .andReturn();
        return extractAccessToken(result.getResponse().getContentAsString());
    }

    private String managerToken() throws Exception {
        return login("alice_johnson@tbank.com", "manager-test-password");
    }

    private void blockClient(Long id) throws Exception {
        mockMvc.perform(patch("/clients/{id}/block", id)
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk());
    }

    private void deactivateClient(Long id) throws Exception {
        mockMvc.perform(patch("/clients/{id}/deactivate", id)
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk());
    }

    private void reactivateClient(Long id) throws Exception {
        mockMvc.perform(patch("/clients/{id}/reactivate", id)
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk());
    }

    private void deleteClient(Long id) throws Exception {
        mockMvc.perform(delete("/clients/{id}", id)
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isNoContent());
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

    private Long extractId(String body) {
        Matcher matcher = Pattern.compile("\"id\":(\\d+)").matcher(body);
        if (!matcher.find()) {
            throw new AssertionError("Response did not include an id");
        }
        return Long.valueOf(matcher.group(1));
    }
}