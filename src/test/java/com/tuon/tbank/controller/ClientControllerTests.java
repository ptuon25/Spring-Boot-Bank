package com.tuon.tbank.controller;


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
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ClientControllerTests {

    private static final AtomicLong COUNTER = new AtomicLong(1000);

    @Autowired
    private MockMvc mockMvc;

    // REGISTER TEST

    @Test
    void postClientCreatesClientAndReturnsLocation() throws Exception {
        String email = uniqueEmail();

        MvcResult result = postRegistration(registrationJson(
                "Location Client", "1990-01-01", uniqueDocument(), email, "a-strong-password-1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Location Client"))
                .andExpect(jsonPath("$.email").value(email))
                .andReturn();

        Long id = extractId(result.getResponse().getContentAsString());
        String location = result.getResponse().getHeader("Location");

        assertNotNull(location);
        assertTrue(location.endsWith("/clients/" + id));
    }

    @Test
    void postClientWorksWithoutToken() throws Exception {
        postRegistration(registrationJson(
                "Public Client", "1990-01-01", uniqueDocument(), uniqueEmail(), "a-strong-password-1"))
                .andExpect(status().isCreated());
    }

    @Test
    void postClientReturnsNormalizedEmail() throws Exception {
        String upperEmail = "Ctrl" + COUNTER.incrementAndGet() + "@TBank.Test";

        postRegistration(registrationJson(
                "Email Client", "1990-01-01", uniqueDocument(), upperEmail, "a-strong-password-1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(upperEmail.toLowerCase(Locale.ROOT)));
    }

    @Test
    void postClientNeverReturnsPasswordOrHash() throws Exception {
        postRegistration(registrationJson(
                "Secret Client", "1990-01-01", uniqueDocument(), uniqueEmail(), "a-strong-password-1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void postClientRejectsBlankFields() throws Exception {
        String password = "a-strong-password-1";

        postRegistration(registrationJson("", "1990-01-01", uniqueDocument(), uniqueEmail(), password))
                .andExpect(status().isBadRequest());
        postRegistration(registrationJson("Blank Doc", "1990-01-01", "", uniqueEmail(), password))
                .andExpect(status().isBadRequest());
        postRegistration(registrationJson("Blank Email", "1990-01-01", uniqueDocument(), "", password))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postClientRejectsInvalidEmail() throws Exception {
        String email = "invalid-email";

        postRegistration(registrationJson("Invalid Email", "1990-01-01", uniqueDocument(), email, "a-strong-password-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postClientRejectsFutureBirthDate() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        postRegistration(registrationJson("Future Birthdate", futureDate.toString(), uniqueDocument(), uniqueEmail(), "a-strong-password-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postClientRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid-json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postClientRejectsDuplicateEmail() throws Exception {
        String email = uniqueEmail();
        postRegistration(registrationJson("First Client", "1990-01-01", uniqueDocument(), email, "a-strong-password-1"))
                .andExpect(status().isCreated());

        postRegistration(registrationJson("Second Client", "1990-01-01", uniqueDocument(), email, "a-strong-password-1"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void postClientRejectsDuplicateDocument() throws Exception {
        String document = uniqueDocument();
        postRegistration(registrationJson("First Client", "1990-01-01", document, uniqueEmail(), "a-strong-password-1"))
                .andExpect(status().isCreated());

        postRegistration(registrationJson("Second Client", "1990-01-01", document, uniqueEmail(), "a-strong-password-1"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void postClientRejectsUnderageClient() throws Exception {
        LocalDate underageBirthDate = LocalDate.now().minusYears(17);

        postRegistration(registrationJson("Underage Client", underageBirthDate.toString(), uniqueDocument(), uniqueEmail(), "a-strong-password-1"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void postClientRejectsPasswordOver72Bytes() throws Exception {
        String longPassword = "a".repeat(73);

        postRegistration(registrationJson("Long Password Client", "1990-01-01", uniqueDocument(), uniqueEmail(), longPassword))
                .andExpect(status().isBadRequest());
    }

    // GET TESTS

    @Test
    void getClientByIdReturns404ForNonexistentClient() throws Exception {
        mockMvc.perform(get("/clients/999999")
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isNotFound());
    }

    @Test
    void getClientByIdReturns404ForDeletedClient() throws Exception {
        TestClient testClient = createClient();

        mockMvc.perform(delete("/clients/" + testClient.id())
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/clients/" + testClient.id())
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isNotFound());
    }

    @Test
    void getClientByIdReturns422ForInvalidId() throws Exception {
        mockMvc.perform(get("/clients/0")
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Client ID must be greater than 0"));
    }

    @Test
    void getClientByIdReturns400ForNonNumericId() throws Exception {
        mockMvc.perform(get("/clients/abc").header("Authorization", bearer(managerToken())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getClientByIdAllowsManagerAndAdmin() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(get("/clients/" + client.id()).header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(client.id()))
                .andExpect(jsonPath("$.name").value("Test Client")).andExpect(jsonPath("$.email").value(client.email()));
        mockMvc.perform(get("/clients/" + client.id()).header("Authorization", bearer(adminToken())))
                .andExpect(status().isOk());
    }

    @Test
    void getClientByIdDeniesAssociate() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(get("/clients/" + client.id()).header("Authorization", bearer(associateToken())))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllClientsReturnsOnlyProfileFields() throws Exception {
        mockMvc.perform(get("/clients").header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists()).andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist()).andExpect(jsonPath("$[0].*", hasSize(3)));
    }

    @Test
    void getAllClientsAllowsAssociateDeniesClient() throws Exception {
        mockMvc.perform(get("/clients").header("Authorization", bearer(associateToken())))
                .andExpect(status().isOk());
        TestClient client = createClient();
        mockMvc.perform(get("/clients").header("Authorization", bearer(login(client.email(), client.password()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDeletedClientsReturnsDeletedClient() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(delete("/clients/" + client.id()).header("Authorization", bearer(managerToken())))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/clients/deleted").header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].id", hasItem(client.id().intValue())));
    }

    @Test
    void getDeletedClientsDeniesAssociateAndClient() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(get("/clients/deleted").header("Authorization", bearer(associateToken())))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/clients/deleted").header("Authorization", bearer(login(client.email(), client.password()))))
                .andExpect(status().isForbidden());
    }

    // SEARCH TEST

    @Test
    void searchByNameReturnsMatches() throws Exception {
        String name = "Search Client " + COUNTER.incrementAndGet();
        TestClient client = createClient(name);
        mockMvc.perform(get("/clients/search/name").param("name", name)
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].id", hasItem(client.id().intValue())));
    }

    @Test
    void searchByNameReturns404WhenNoMatch() throws Exception {
        mockMvc.perform(get("/clients/search/name").param("name", "Unknown " + COUNTER.incrementAndGet())
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchByNameReturns422WhenBlank() throws Exception {
        mockMvc.perform(get("/clients/search/name").param("name", " ")
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Client name cannot be null or blank"));
    }

    @Test
    void searchByNameReturns400WhenParamMissing() throws Exception {
        mockMvc.perform(get("/clients/search/name").header("Authorization", bearer(managerToken())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchByEmailIgnoresCaseAndSpaces() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(get("/clients/search/email").param("email", "  " + client.email().toUpperCase(Locale.ROOT) + "  ")
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(client.email()));
    }

    @Test
    void searchByEmailReturns404ForUnknownEmail() throws Exception {
        mockMvc.perform(get("/clients/search/email").param("email", uniqueEmail())
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchByStatusReturnsOnlyRequestedStatus() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(delete("/clients/" + client.id()).header("Authorization", bearer(managerToken())))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/clients/search/status").param("status", "ACTIVE")
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].id", not(hasItem(client.id().intValue()))));
    }

    @Test
    void searchByStatusRejectsInvalidValue() throws Exception {
        mockMvc.perform(get("/clients/search/status").param("status", "XYZ")
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchByDocumentReturnsClientForManager() throws Exception {
        DetailedClient client = createDetailedClient("Document Client");
        mockMvc.perform(get("/clients/search/document").param("document", client.document())
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(client.id()));
    }

    @Test
    void searchByDocumentAcceptsFormattedDocument() throws Exception {
        DetailedClient client = createDetailedClient("Formatted Document");
        String document = client.document().substring(0, 4) + "." + client.document().substring(4);
        mockMvc.perform(get("/clients/search/document").param("document", document)
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(client.id()));
    }

    @Test
    void searchByDocumentDeniesAssociate() throws Exception {
        DetailedClient client = createDetailedClient("Protected Document");
        mockMvc.perform(get("/clients/search/document").param("document", client.document())
                        .header("Authorization", bearer(associateToken())))
                .andExpect(status().isForbidden());
    }

    // UPDATE TEST

    @Test
    void putClientUpdatesAndReturnsProfile() throws Exception {
        TestClient client = createClient();
        String name = "Updated " + COUNTER.incrementAndGet();
        String email = uniqueEmail();
        mockMvc.perform(put("/clients/" + client.id()).header("Authorization", bearer(managerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson(name, "1991-02-02", uniqueDocument(), email)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void putClientPersistsChanges() throws Exception {
        TestClient client = createClient();
        String name = "Persisted " + COUNTER.incrementAndGet();
        String email = uniqueEmail();
        mockMvc.perform(put("/clients/" + client.id()).header("Authorization", bearer(managerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson(name, "1991-02-02", uniqueDocument(), email)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/clients/" + client.id()).header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void putClientIgnoresStatusAndPasswordInBody() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(put("/clients/" + client.id()).header("Authorization", bearer(managerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJsonWithExtras("Updated Extras", "1991-02-02", uniqueDocument(), client.email())))
                .andExpect(status().isOk());
        login(client.email(), client.password());
    }

    @Test
    void putClientAllowsAdmin() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(put("/clients/" + client.id()).header("Authorization", bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson("Admin Updated", "1991-02-02", uniqueDocument(), uniqueEmail())))
                .andExpect(status().isOk());
    }

    @Test
    void putClientDeniesClient() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(put("/clients/" + client.id()).header("Authorization", bearer(login(client.email(), client.password())))
                        .contentType(MediaType.APPLICATION_JSON).content(updateJson("Denied", "1991-02-02", uniqueDocument(), uniqueEmail())))
                .andExpect(status().isForbidden());
    }

    @Test
    void putClientRejectsDuplicateEmail() throws Exception {
        TestClient first = createClient();
        TestClient second = createClient();
        mockMvc.perform(put("/clients/" + second.id()).header("Authorization", bearer(managerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson("Duplicate Email", "1991-02-02", uniqueDocument(), first.email())))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("This e-mail is already registered by another client"));
    }

    @Test
    void putClientRejectsEmptyBody() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(put("/clients/" + client.id()).header("Authorization", bearer(managerToken()))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed."));
    }

    @Test
    void putClientRejectsUnderageBirthDate() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(put("/clients/" + client.id()).header("Authorization", bearer(managerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson("Underage", LocalDate.now().minusYears(17).toString(), uniqueDocument(), uniqueEmail())))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Client must be at least 18 years old"));
    }

    @Test
    void putClientReturns404ForDeletedClient() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(delete("/clients/" + client.id()).header("Authorization", bearer(managerToken())))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/clients/" + client.id()).header("Authorization", bearer(managerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson("Deleted", "1991-02-02", uniqueDocument(), uniqueEmail())))
                .andExpect(status().isNotFound());
    }

    // STATUS TEST

    @Test
    void patchBlockReturnsProfileForManager() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(patch("/clients/" + client.id() + "/block").header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(client.id()))
                .andExpect(jsonPath("$.name").value("Test Client")).andExpect(jsonPath("$.email").value(client.email()));
    }

    @Test
    void patchBlockRejectsAlreadyBlockedClient() throws Exception {
        TestClient client = createClient();
        String path = "/clients/" + client.id() + "/block";
        mockMvc.perform(patch(path).header("Authorization", bearer(managerToken()))).andExpect(status().isOk());
        mockMvc.perform(patch(path).header("Authorization", bearer(managerToken()))).andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Client is already blocked"));
    }

    @Test
    void patchDeactivateReturnsProfileForManager() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(patch("/clients/" + client.id() + "/deactivate").header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(client.id()));
    }

    @Test
    void patchDeactivateRejectsBlockedClient() throws Exception {
        TestClient client = createClient();
        String base = "/clients/" + client.id();
        mockMvc.perform(patch(base + "/block").header("Authorization", bearer(managerToken()))).andExpect(status().isOk());
        mockMvc.perform(patch(base + "/deactivate").header("Authorization", bearer(managerToken())))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Cannot deactivate a blocked client"));
    }

    @Test
    void patchReactivateReturnsProfileForManager() throws Exception {
        TestClient client = createClient();
        String base = "/clients/" + client.id();
        mockMvc.perform(patch(base + "/deactivate").header("Authorization", bearer(managerToken()))).andExpect(status().isOk());
        mockMvc.perform(patch(base + "/reactivate").header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(client.id()));
    }

    @Test
    void patchReactivateRejectsActiveClient() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(patch("/clients/" + client.id() + "/reactivate").header("Authorization", bearer(managerToken())))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Client is already active"));
    }

    @Test
    void patchStatusAllowsAdmin() throws Exception {
        TestClient blocked = createClient();
        String blockedPath = "/clients/" + blocked.id();
        mockMvc.perform(patch(blockedPath + "/block").header("Authorization", bearer(adminToken())))
                .andExpect(status().isOk());
        mockMvc.perform(patch(blockedPath + "/reactivate").header("Authorization", bearer(adminToken())))
                .andExpect(status().isOk());
        TestClient inactive = createClient();
        mockMvc.perform(patch("/clients/" + inactive.id() + "/deactivate").header("Authorization", bearer(adminToken())))
                .andExpect(status().isOk());
    }

    @Test
    void patchStatusDeniesAssociateAndClient() throws Exception {
        TestClient client = createClient();
        String base = "/clients/" + client.id();
        String associate = bearer(associateToken());
        String own = bearer(login(client.email(), client.password()));
        for (String route : new String[]{"/block", "/deactivate", "/reactivate"}) {
            mockMvc.perform(patch(base + route).header("Authorization", associate)).andExpect(status().isForbidden());
            mockMvc.perform(patch(base + route).header("Authorization", own)).andExpect(status().isForbidden());
        }
    }

    @Test
    void patchStatusReturns404ForNonexistentClient() throws Exception {
        mockMvc.perform(patch("/clients/" + Long.MAX_VALUE + "/block")
                .header("Authorization", bearer(managerToken()))).andExpect(status().isNotFound());
    }

    // DELETE TEST

    @Test
    void deleteClientReturns204() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(delete("/clients/" + client.id()).header("Authorization", bearer(managerToken())))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
    }

    @Test
    void deleteClientHidesClientFromGetById() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(delete("/clients/" + client.id()).header("Authorization", bearer(managerToken())))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/clients/" + client.id()).header("Authorization", bearer(managerToken())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteClientRejectsSecondDeletion() throws Exception {
        TestClient client = createClient();
        String path = "/clients/" + client.id();
        mockMvc.perform(delete(path).header("Authorization", bearer(managerToken()))).andExpect(status().isNoContent());
        mockMvc.perform(delete(path).header("Authorization", bearer(managerToken()))).andExpect(status().isNotFound());
    }

    @Test
    void deleteClientAllowsAdmin() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(delete("/clients/" + client.id()).header("Authorization", bearer(adminToken())))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteClientDeniesClient() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(delete("/clients/" + client.id())
                        .header("Authorization", bearer(login(client.email(), client.password()))))
                .andExpect(status().isForbidden());
    }

    // PASSWORD TEST

    @Test
    void patchOwnPasswordReturns204WithEmptyBody() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(patch("/clients/" + client.id() + "/password")
                        .header("Authorization", bearer(login(client.email(), client.password())))
                        .contentType(MediaType.APPLICATION_JSON).content(passwordChangeJson(client.password(), "new-password-123")))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
    }

    @Test
    void patchOwnPasswordAllowsLoginWithNewPassword() throws Exception {
        TestClient client = createClient();
        changePassword(client);
        login(client.email(), "new-password-123");
    }

    @Test
    void patchOwnPasswordRejectsOldPasswordAfterChange() throws Exception {
        TestClient client = createClient();
        changePassword(client);
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(client.email(), client.password())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patchOwnPasswordRejectsWrongCurrentPassword() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(patch("/clients/" + client.id() + "/password")
                        .header("Authorization", bearer(login(client.email(), client.password())))
                        .contentType(MediaType.APPLICATION_JSON).content(passwordChangeJson("wrong-password", "new-password-123")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    void patchOwnPasswordRejectsSamePassword() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(patch("/clients/" + client.id() + "/password")
                        .header("Authorization", bearer(login(client.email(), client.password())))
                        .contentType(MediaType.APPLICATION_JSON).content(passwordChangeJson(client.password(), client.password())))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("New password must be different from the current password"));
    }

    @Test
    void patchOwnPasswordRejectsShortNewPassword() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(patch("/clients/" + client.id() + "/password")
                        .header("Authorization", bearer(login(client.email(), client.password())))
                        .contentType(MediaType.APPLICATION_JSON).content(passwordChangeJson(client.password(), "short")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchOwnPasswordDeniesOtherClient() throws Exception {
        TestClient first = createClient();
        TestClient second = createClient();
        mockMvc.perform(patch("/clients/" + first.id() + "/password")
                        .header("Authorization", bearer(login(second.email(), second.password())))
                        .contentType(MediaType.APPLICATION_JSON).content(passwordChangeJson(first.password(), "new-password-123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void patchOwnPasswordDeniesEmployees() throws Exception {
        TestClient client = createClient();
        String path = "/clients/" + client.id() + "/password";
        String body = passwordChangeJson(client.password(), "new-password-123");
        mockMvc.perform(patch(path).header("Authorization", bearer(managerToken()))
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mockMvc.perform(patch(path).header("Authorization", bearer(adminToken()))
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
    }

    @Test
    void patchResetPasswordReturns204ForManager() throws Exception {
        TestClient client = createClient();
        resetPassword(client, managerToken()).andExpect(status().isNoContent());
    }

    @Test
    void patchResetPasswordReturns204ForAdmin() throws Exception {
        TestClient client = createClient();
        resetPassword(client, adminToken()).andExpect(status().isNoContent());
    }

    @Test
    void patchResetPasswordAllowsLoginWithNewPassword() throws Exception {
        TestClient client = createClient();
        resetPassword(client, managerToken()).andExpect(status().isNoContent());
        login(client.email(), "reset-password-123");
    }

    @Test
    void patchResetPasswordDeniesAssociateAndClient() throws Exception {
        TestClient client = createClient();
        String path = "/clients/" + client.id() + "/password/reset";
        String body = passwordResetJson("reset-password-123");
        mockMvc.perform(patch(path).header("Authorization", bearer(associateToken()))
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mockMvc.perform(patch(path).header("Authorization", bearer(login(client.email(), client.password())))
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
    }

    @Test
    void patchResetPasswordRejectsShortPassword() throws Exception {
        TestClient client = createClient();
        mockMvc.perform(patch("/clients/" + client.id() + "/password/reset")
                .header("Authorization", bearer(managerToken())).contentType(MediaType.APPLICATION_JSON)
                .content(passwordResetJson("short"))).andExpect(status().isBadRequest());
    }

    @Test
    void patchResetPasswordReturns404ForNonexistentClient() throws Exception {
        mockMvc.perform(patch("/clients/" + Long.MAX_VALUE + "/password/reset")
                .header("Authorization", bearer(managerToken())).contentType(MediaType.APPLICATION_JSON)
                .content(passwordResetJson("reset-password-123"))).andExpect(status().isNotFound());
    }

    // SECURITY TEST

    @Test
    void clientRoutesRejectMissingToken() throws Exception {
        String[] paths = {"/clients", "/clients/1", "/clients/deleted", "/clients/search/name",
                "/clients/1", "/clients/1", "/clients/1/block", "/clients/1/deactivate",
                "/clients/1/reactivate", "/clients/1/password", "/clients/1/password/reset"};
        for (String path : paths) {
            if (path.equals("/clients")) {
                mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
            } else if (path.equals("/clients/search/name")) {
                mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
            } else if (path.endsWith("/block") || path.endsWith("/deactivate") || path.endsWith("/reactivate")
                    || path.endsWith("/password") || path.endsWith("/password/reset")) {
                mockMvc.perform(patch(path)).andExpect(status().isUnauthorized());
            } else if (path.equals("/clients/1")) {
                mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
                mockMvc.perform(put(path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                        .andExpect(status().isUnauthorized());
                mockMvc.perform(delete(path)).andExpect(status().isUnauthorized());
            } else {
                mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
            }
        }
    }

    @Test
    void clientRoutesRejectMalformedToken() throws Exception {
        mockMvc.perform(get("/clients").header("Authorization", "Bearer malformed.token.value"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/clients/1").header("Authorization", "Bearer malformed.token.value"))
                .andExpect(status().isUnauthorized());
    }

    // ERROR FORMAT TEST

    @Test
    void errorResponsesUseStandardErrorFormat() throws Exception {
        mockMvc.perform(get("/clients/" + Long.MAX_VALUE).header("Authorization", bearer(managerToken())))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404)).andExpect(jsonPath("$.error").value("Resource not found"))
                .andExpect(jsonPath("$.message").exists()).andExpect(jsonPath("$.path").value("/clients/" + Long.MAX_VALUE));
        TestClient first = createClient();
        TestClient second = createClient();
        mockMvc.perform(put("/clients/" + second.id()).header("Authorization", bearer(managerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson("Duplicate", "1991-02-02", uniqueDocument(), first.email())))
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(422)).andExpect(jsonPath("$.error").value("Business rule violation"))
                .andExpect(jsonPath("$.message").exists()).andExpect(jsonPath("$.path").value("/clients/" + second.id()));
    }


    // AUXILIARY

    private static String uniqueDocument() {
        return "2000" + COUNTER.incrementAndGet();
    }

    private static String uniqueEmail() {
        return "ctrl" + COUNTER.incrementAndGet() + "@tbank.test";
    }

    private String registrationJson(String name, String birthDate, String document,
                                    String email, String password) {
        return """
                {"name":"%s","birthDate":"%s","document":"%s","email":"%s","password":"%s"}
                """.formatted(name, birthDate, document, email, password);
    }

    private ResultActions postRegistration(String json) throws Exception {
        return mockMvc.perform(post("/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private record TestClient(Long id, String email, String password) {
    }

    private TestClient createClient() throws Exception {
        return createClient("Test Client");
    }

    private TestClient createClient(String name) throws Exception {
        String email = uniqueEmail();
        String password = "a-strong-password-1";
        MvcResult result = postRegistration(registrationJson(
                name, "1990-01-01", uniqueDocument(), email, password))
                .andExpect(status().isCreated())
                .andReturn();
        return new TestClient(extractId(result.getResponse().getContentAsString()), email, password);
    }

    private record DetailedClient(Long id, String email, String password, String document) {
    }

    private DetailedClient createDetailedClient(String name) throws Exception {
        String email = uniqueEmail();
        String password = "a-strong-password-1";
        String document = uniqueDocument();
        MvcResult result = postRegistration(registrationJson(name, "1990-01-01", document, email, password))
                .andExpect(status().isCreated()).andReturn();
        return new DetailedClient(extractId(result.getResponse().getContentAsString()), email, password, document);
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

    private String loginJson(String email, String password) {
        return """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
    }

    private void changePassword(TestClient client) throws Exception {
        mockMvc.perform(patch("/clients/" + client.id() + "/password")
                        .header("Authorization", bearer(login(client.email(), client.password())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordChangeJson(client.password(), "new-password-123")))
                .andExpect(status().isNoContent());
    }

    private ResultActions resetPassword(TestClient client, String token) throws Exception {
        return mockMvc.perform(patch("/clients/" + client.id() + "/password/reset")
                .header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
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
