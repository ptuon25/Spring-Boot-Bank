package com.tuon.tbank.controller;

import com.tuon.tbank.enums.person.EmployeeRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EmployeeControllerTests {

    private static final AtomicLong COUNTER = new AtomicLong(1000);

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerEmployeeReturnsCreated() throws Exception {
        String token = adminToken();
        String name = uniqueName();
        String birthDate = LocalDate.of(1980, 1, 1).toString();
        String document = uniqueDocument();
        String email = uniqueEmail();
        String password = "test-password";
        String role = EmployeeRole.MANAGER.name();

        String json = registrationJson(name, birthDate, document, email, password, role);

        MvcResult result = postRegistration(json)
                .andExpect(status().isUnauthorized())
                .andReturn();
        result = mockMvc.perform(post("/employees")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        String location = result.getResponse().getHeader("Location");

        Long id = extractId(result.getResponse().getContentAsString());
        assertNotNull(id);
        assertNotNull(location);
        assertTrue(location.endsWith("/employees/" + id));
    }

    @Test
    void registerEmployeeRejectsInvalidRequest() throws Exception {
        String name = "";
        String birthDate = "invalid-date";
        String document = "invalid-document";
        String email = "invalid-email";
        String password = "test-password";
        String role = "INVALID_ROLE";

        String json = registrationJson(name, birthDate, document, email, password, role);

        MvcResult result = mockMvc.perform(post("/employees")
                .header("Authorization", bearer(adminToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertNull(result.getResponse().getHeader("Location"));
    }

    @Test
    void searchEndpointsReturnEmployees() throws Exception {
        String token = adminToken();
        TestEmployee employee = createEmployee(uniqueName(), EmployeeRole.ASSOCIATE);

        assertAll(
                () -> mockMvc.perform(get("/employees").header("Authorization", bearer(token)))
                        .andExpect(status().isOk()),
                () -> mockMvc.perform(get("/employees/active").header("Authorization", bearer(token)))
                        .andExpect(status().isOk()),
                () -> mockMvc.perform(get("/employees/" + employee.id())
                                .header("Authorization", bearer(token)))
                        .andExpect(status().isOk()),
                () -> mockMvc.perform(get("/employees/email").param("email", employee.email())
                                .header("Authorization", bearer(token)))
                        .andExpect(status().isOk()),
                () -> mockMvc.perform(get("/employees/document").param("document", employee.document())
                                .header("Authorization", bearer(token)))
                        .andExpect(status().isOk()),
                () -> mockMvc.perform(get("/employees/role").param("role", employee.role().name())
                                .header("Authorization", bearer(token)))
                        .andExpect(status().isOk()),
                () -> mockMvc.perform(get("/employees/status").param("status", "ACTIVE")
                                .header("Authorization", bearer(token)))
                        .andExpect(status().isOk())
        );
    }

    @Test
    void searchEndpointsRespectFilters() throws Exception {
        String token = adminToken();
        TestEmployee employee = createEmployee(uniqueName(), EmployeeRole.ASSOCIATE);

        assertAll(
                () -> mockMvc.perform(get("/employees/birthdates")
                                .param("startDate", "1970-01-01")
                                .param("endDate", "2000-01-01")
                                .header("Authorization", bearer(token)))
                        .andExpect(status().isOk()),
                () -> mockMvc.perform(get("/employees/admission-date")
                                .param("startDate", LocalDate.now().minusDays(1).toString())
                                .param("endDate", LocalDate.now().plusDays(1).toString())
                                .header("Authorization", bearer(token)))
                        .andExpect(status().isOk()),
                () -> mockMvc.perform(get("/employees/role-status")
                                .param("role", employee.role().name())
                                .param("status", "ACTIVE")
                                .header("Authorization", bearer(token)))
                        .andExpect(status().isOk()),
                () -> mockMvc.perform(get("/employees/email/all")
                                .param("email", employee.email())
                                .header("Authorization", bearer(token)))
                        .andExpect(status().isOk())
        );
    }

    @Test
    void updateEmployeeReturnsUpdatedProfile() throws Exception {
        String token = adminToken();
        TestEmployee employee = createEmployee(uniqueName(), EmployeeRole.ASSOCIATE);

        mockMvc.perform(put("/employees/" + employee.id())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson("Updated Employee", "1985-02-02",
                                uniqueDocument(), uniqueEmail())))
                .andExpect(status().isOk());
    }

    @Test
    void employeeManagementEndpointsUpdateState() throws Exception {
        String token = adminToken();
        TestEmployee employee = createEmployee(uniqueName(), EmployeeRole.ADMIN);

        assertAll(
                () -> mockMvc.perform(patch("/employees/" + employee.id() + "/salary")
                                .header("Authorization", bearer(token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(salaryJson("5000.00")))
                        .andExpect(status().isOk()),
                () -> mockMvc.perform(patch("/employees/" + employee.id() + "/role")
                                .header("Authorization", bearer(token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(roleJson("MANAGER")))
                        .andExpect(status().isOk()),
                () -> mockMvc.perform(patch("/employees/" + employee.id() + "/leave")
                                .header("Authorization", bearer(token)))
                        .andExpect(status().isOk()),
                () -> mockMvc.perform(patch("/employees/" + employee.id() + "/reactivate")
                                .header("Authorization", bearer(token)))
                        .andExpect(status().isOk()),
                () -> mockMvc.perform(patch("/employees/" + employee.id() + "/terminate")
                                .header("Authorization", bearer(token)))
                        .andExpect(status().isOk())
        );
    }

    @Test
    void deleteEmployeeReturnsNoContent() throws Exception {
        String token = adminToken();
        TestEmployee employee = createEmployee(uniqueName(), EmployeeRole.ADMIN);

        mockMvc.perform(delete("/employees/" + employee.id())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void employeeCanChangeOwnPassword() throws Exception {
        TestEmployee employee = createEmployee(uniqueName(), EmployeeRole.ADMIN);

        changePassword(employee);
    }

    @Test
    void adminCanResetEmployeePassword() throws Exception {
        String token = adminToken();
        TestEmployee employee = createEmployee(uniqueName(), EmployeeRole.ASSOCIATE);

        resetPassword(employee, token).andExpect(status().isNoContent());
    }

    @Test
    void associateCannotManageEmployees() throws Exception {
        TestEmployee associate = createEmployee(uniqueName(), EmployeeRole.ASSOCIATE);
        String token = login(associate.email(), associate.password());

        mockMvc.perform(post("/employees")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(uniqueName(), "1980-01-01",
                                uniqueDocument(), uniqueEmail(), "test-password", "ASSOCIATE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/employees/" + associate.id())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
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

    private String registrationJson(String name, String birthDate, String document,
                                    String email, String password, String role) {
        return """
                {"name": "%s", "birthDate": "%s", "document": "%s", "email": "%s", "password": "%s", "role": "%s"}
                """.formatted(name, birthDate, document, email, password, role);
    }

    private String salaryJson(String salary) {
        return """
                {"salary": %s}
                """.formatted(salary);
    }

    private String roleJson(String role) {
        return """
                {"role": "%s"}
                """.formatted(role);
    }

    private ResultActions postRegistration(String json) throws Exception {
        return mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions postRegistration(String json, String token) throws Exception {
        return mockMvc.perform(post("/employees")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private record TestEmployee(Long id, String email, String password, String document, EmployeeRole role) {
    }

    private TestEmployee createEmployee(String name, EmployeeRole role) throws Exception {
        String email = uniqueEmail();
        String password = "test-password";
        String document = uniqueDocument();
        return createEmployee(name, role, email, password, document);
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

    private TestEmployee createEmployee(String name, EmployeeRole role,
                                        String email, String password, String document) throws Exception {
        String json = registrationJson(name, LocalDate.of(1980, 1, 1).toString(),
                document, email, password, role.name());
        MvcResult result = postRegistration(json, adminToken())
                .andExpect(status().isCreated())
                .andReturn();
        Long id = extractId(result.getResponse().getContentAsString());
        return new TestEmployee(id, email, password, document, role);
    }

    private String updateJson(String name, String birthDate, String document, String email) {
        return """
                {"name":"%s","birthDate":"%s","document":"%s","email":"%s"}
                """.formatted(name, birthDate, document, email);
    }

    private String updateJsonWithExtras(String name, String birthDate, String document, String email) {
        return """
                {"name":"%s","birthDate":"%s","document":"%s","email":"%s","status":"BLOCKED","passwordHash":"x"}
                """.formatted(name, birthDate, document, email);
    }

    private String passwordChangeJson(String currentPassword, String newPassword) {
        return """
                {"currentPassword":"%s","newPassword":"%s"}
                """.formatted(currentPassword, newPassword);
    }

    private String passwordResetJson(String password) {
        return """
                {"newPassword":"%s"}
                """.formatted(password);
    }

    private void changePassword(TestEmployee employee) throws Exception {
        mockMvc.perform(patch("/employees/" + employee.id() + "/password")
                        .header("Authorization", bearer(login(employee.email(), employee.password())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordChangeJson(employee.password(), "new-password-123")))
                .andExpect(status().isNoContent());
    }

    private ResultActions resetPassword(TestEmployee employee, String token) throws Exception {
        return mockMvc.perform(patch("/employees/" + employee.id() + "/password/reset")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(passwordResetJson("reset-password-123")));
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

    private String managerToken() throws Exception {
        return login("alice_johnson@tbank.com", "manager-test-password");
    }

    private String associateToken() throws Exception {
        return login("bob_williams@tbank.com", "associate-test-password");
    }

    private String adminToken() throws Exception {
        return login("admin@tbank.test", "admin-test-password");
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
