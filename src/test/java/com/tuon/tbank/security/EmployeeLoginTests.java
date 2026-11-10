package com.tuon.tbank.security;

import com.tuon.tbank.enums.person.EmployeeRole;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EmployeeLoginTests {

    private static final AtomicLong COUNTER = new AtomicLong(7000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtDecoder jwtDecoder;

    // LOGIN SUCCESS TEST

    @Test
    void activeEmployeeCanLogin() throws Exception {
        TestEmployee employee = createAdmin("Admin");

        attemptLogin(employee.email(), employee.password())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void loginTokenContainsEmployeeIdentity() throws Exception {
        TestEmployee employee = createAssociate("Associate");

        Jwt jwt = jwtDecoder.decode(login(employee.email(), employee.password()));

        assertNotNull(jwt);
        assertEquals("EMPLOYEE", jwt.getClaim("identity_type"));
        assertEquals(employee.id().toString(), Objects.requireNonNull(jwt.getClaim("user_id")).toString());
        assertEquals(List.of("ROLE_ASSOCIATE"), jwt.getClaimAsStringList("authorities"));
    }

    @Test
    void reactivatedEmployeeCanLoginAgain() throws Exception {
        TestEmployee employee = createManager("Manager");
        putEmployeeOnLeave(employee.id());
        reactivateEmployee(employee.id());

        attemptLogin(employee.email(), employee.password())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    // LOGIN REJECTION TEST

    @Test
    void onLeaveEmployeeCannotLogin() throws Exception {
        TestEmployee employee = createManager("Manager");
        putEmployeeOnLeave(employee.id());

        attemptLogin(employee.email(), employee.password())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void terminatedEmployeeCannotLogin() throws Exception {
        TestEmployee employee = createManager("Manager");
        terminateEmployee(employee.id());

        attemptLogin(employee.email(), employee.password())
                .andExpect(status().isUnauthorized());
    }

    // EMAIL FORMAT TEST

    @Test
    void loginRejectsInvalidEmailFormat() throws Exception {
        String invalidEmail = "invalid-email-format";
        String password = "test-password";

        attemptLogin(invalidEmail, password)
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginRejectsSpacesAroundEmail() throws Exception {
        TestEmployee employee = createAdmin("Admin");

        attemptLogin(" " + employee.email() + " ", employee.password())
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithUppercaseEmailBehavior() throws Exception {
        TestEmployee employee = createAdmin("Admin");

        attemptLogin(employee.email().toUpperCase(Locale.ROOT), employee.password())
                .andExpect(status().isUnauthorized());
    }

    // KNOWN LIMIT TEST

    @Test
    void oldTokenStillWorksAfterPasswordChange() throws Exception {
        TestEmployee employee = createAssociate("Associate");
        String oldToken = login(employee.email(), employee.password());

        String newPassword = "new-test-password";
        mockMvc.perform(patch("/employees/{id}/password", employee.id())
                        .header("Authorization", bearer(oldToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"%s","newPassword":"%s"}
                                """.formatted(employee.password(), newPassword)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/employees/{id}", employee.id())
                        .header("Authorization", bearer(oldToken)))
                .andExpect(status().isOk());
    }

    @Test
    void oldTokenStillWorksAfterTerminate() throws Exception {
        TestEmployee employee = createAssociate("Associate");
        String oldToken = login(employee.email(), employee.password());

        terminateEmployee(employee.id());

        mockMvc.perform(get("/employees/{id}", employee.id())
                        .header("Authorization", bearer(oldToken)))
                .andExpect(status().isOk());
    }

    @Test
    void oldTokenStillWorksAfterOnLeave() throws Exception {
        TestEmployee employee = createAssociate("Associate");
        String oldToken = login(employee.email(), employee.password());

        putEmployeeOnLeave(employee.id());

        mockMvc.perform(get("/employees/{id}", employee.id())
                        .header("Authorization", bearer(oldToken)))
                .andExpect(status().isOk());
    }

    // AUXILIARY

    private static String uniqueDocument() {
        return "2000" + COUNTER.incrementAndGet();
    }

    private static String uniqueEmail() {
        return "ctrl" + COUNTER.incrementAndGet() + "@tbank.test";
    }

    private static String uniqueName() {
        return "Ctrl Employee " + COUNTER.incrementAndGet();
    }

    private record TestEmployee(Long id, String email, String password, String document, EmployeeRole role) {
    }

    private TestEmployee createEmployee(String name, EmployeeRole role) throws Exception {
        String empName = uniqueName();
        name = empName;
        String email = uniqueEmail();
        String password = "test-password";
        String document = uniqueDocument();
        MvcResult result = mockMvc.perform(post("/employees")
                        .header("Authorization", bearer(seedAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","birthDate":"1990-01-01","email":"%s","password":"%s","document":"%s","role":"%s"}
                                """.formatted(empName, email, password, document, role)))
                .andExpect(status().isCreated())
                .andReturn();
        return new TestEmployee(extractId(result.getResponse().getContentAsString()), email, password, document, role);
    }

    private TestEmployee createAdmin(String name) throws Exception {
        return createEmployee(name, EmployeeRole.ADMIN);
    }

    private TestEmployee createManager(String name) throws Exception {
        return createEmployee(name, EmployeeRole.MANAGER);
    }

    private TestEmployee createAssociate(String name) throws Exception {
        return createEmployee(name, EmployeeRole.ASSOCIATE);
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = attemptLogin(email, password)
                .andExpect(status().isOk())
                .andReturn();
        return extractAccessToken(result.getResponse().getContentAsString());
    }

    private ResultActions attemptLogin(String email, String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password)));
    }

    private String adminToken() throws Exception {
        TestEmployee admin = createAdmin("Admin");
        return login(admin.email(), admin.password());
    }

    private String seedAdminToken() throws Exception {
        return login("admin@tbank.test", "admin-test-password");
    }

    private String managerToken() throws Exception {
        TestEmployee manager = createManager("Manager");
        return login(manager.email(), manager.password());
    }

    private String associateToken() throws Exception {
        TestEmployee associate = createAssociate("Associate");
        return login(associate.email(), associate.password());
    }

    private void putEmployeeOnLeave(Long id) throws Exception {
        mockMvc.perform(patch("/employees/{id}/leave", id)
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk());
    }

    private void terminateEmployee(Long id) throws Exception {
        mockMvc.perform(patch("/employees/{id}/terminate", id)
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk());
    }

    private void reactivateEmployee(Long id) throws Exception {
        mockMvc.perform(patch("/employees/{id}/reactivate", id)
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk());
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
