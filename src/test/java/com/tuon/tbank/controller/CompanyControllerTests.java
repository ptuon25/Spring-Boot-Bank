package com.tuon.tbank.controller;

import com.tuon.tbank.enums.company.CompanyMemberRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CompanyControllerTests {

    private static final AtomicLong COUNTER = new AtomicLong(1000);

    @Autowired
    private MockMvc mockMvc;

    // COMPANY TESTS

    @Test
    void registerCompanyReturnsCreated() throws Exception {
        TestClient partner = createPartner();
        String registrationNumber = uniqueRegistrationNumber();

        MvcResult result = postRegistration(registrationJson(
                        uniqueLegalName(), uniqueTradeName(), registrationNumber, partner.id(), "2023-01-01"),
                partner)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.registrationNumber").value(registrationNumber))
                .andExpect(header().exists("Location"))
                .andReturn();

        Long id = extractId(result.getResponse().getContentAsString());
        assertTrue(result.getResponse().getHeader("Location").endsWith("/companies/" + id));
    }

    @Test
    void registerCompanyRejectsInvalidRequest() throws Exception {
        TestClient partner = createPartner();

        postRegistration(registrationJson("", "", "", 0L, "2099-01-01"), partner)
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void searchCompanyEndpointsReturnOk() throws Exception {
        TestClient partner = createPartner();
        TestCompany company = createCompany(partner);
        String token = managerToken();

        mockMvc.perform(get("/companies").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/companies/" + company.id()).header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/companies/search/partner").param("partnerId", partner.id().toString())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/companies/search/registration-number")
                        .param("registrationNumber", company.registrationNumber())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/companies/search/foundation-date-range")
                        .param("startDate", "2020-01-01").param("endDate", "2025-01-01")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/companies/search/affiliation-date-range")
                        .param("startDate", "2020-01-01").param("endDate", "2025-01-01")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/companies/search/status").param("status", "ACTIVE")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void updateCompanyReturnsUpdatedProfile() throws Exception {
        TestClient partner = createPartner();
        TestCompany company = createCompany(partner);

        mockMvc.perform(put("/companies/" + company.id())
                        .header("Authorization", bearer(managerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson(uniqueLegalName(), uniqueTradeName(),
                                partner.id(), uniqueRegistrationNumber())))
                .andExpect(status().isOk());
    }

    @Test
    void updateCompanyFoundationDateReturnsUpdatedProfile() throws Exception {
        TestClient partner = createPartner();
        TestCompany company = createCompany(partner);

        mockMvc.perform(put("/companies/" + company.id() + "/foundation-date")
                        .header("Authorization", bearer(managerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(foundationDateJson("2022-02-02")))
                .andExpect(status().isOk());
    }

    @Test
    void companyManagementEndpointsUpdateState() throws Exception {
        TestClient partner = createPartner();
        String token = managerToken();

        TestCompany blocked = createCompany(partner);
        mockMvc.perform(patch("/companies/" + blocked.id() + "/block")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        TestCompany inactive = createCompany(partner);
        mockMvc.perform(patch("/companies/" + inactive.id() + "/deactivate")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/companies/" + inactive.id() + "/reactivate")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCompanyReturnsNoContent() throws Exception {
        TestClient partner = createPartner();
        TestCompany company = createCompany(partner);

        mockMvc.perform(delete("/companies/" + company.id())
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/companies/deleted")
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk());
    }

    @Test
    void associateCannotManageCompanies() throws Exception {
        TestClient partner = createPartner();
        TestCompany company = createCompany(partner);
        String token = associateToken();

        mockMvc.perform(put("/companies/" + company.id())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson(uniqueLegalName(), uniqueTradeName(),
                                partner.id(), uniqueRegistrationNumber())))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/companies/" + company.id())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    // COMPANY MEMBERSHIP TESTS

    @Test
    void addMemberReturnsCreated() throws Exception {
        TestClient partner = createPartner();
        TestCompany company = createCompany(partner);
        TestClient member = createMember();

        MvcResult result = mockMvc.perform(post("/companies/" + company.id() + "/members")
                        .header("Authorization", bearer(managerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(membershipJson(member.id(), CompanyMemberRole.OPERATOR)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.membershipId").isNumber())
                .andExpect(jsonPath("$.clientId").value(member.id()))
                .andExpect(jsonPath("$.role").value("OPERATOR"))
                .andReturn();

        Long membershipId = extractMembershipId(result.getResponse().getContentAsString());
        assertNotNull(membershipId);
    }

    @Test
    void listMembersReturnsActiveMemberships() throws Exception {
        TestClient partner = createPartner();
        TestCompany company = createCompany(partner);
        TestClient member = createMember();
        createMembership(company, member, CompanyMemberRole.OPERATOR);

        mockMvc.perform(get("/companies/" + company.id() + "/members")
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].clientId").value(hasItem(member.id().intValue())))
                .andExpect(jsonPath("$[*].role").value(hasItem("OPERATOR")))
                .andExpect(jsonPath("$[*].status").value(hasItem("ACTIVE")));
    }

    @Test
    void changeMemberRoleReturnsUpdatedMembership() throws Exception {
        TestClient partner = createPartner();
        TestCompany company = createCompany(partner);
        TestClient member = createMember();
        TestMembership membership = createMembership(company, member, CompanyMemberRole.OPERATOR);

        mockMvc.perform(patch("/companies/" + company.id() + "/members/" + membership.id() + "/role")
                        .header("Authorization", bearer(managerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson(CompanyMemberRole.VIEWER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.membershipId").value(membership.id()))
                .andExpect(jsonPath("$.clientId").value(member.id()))
                .andExpect(jsonPath("$.role").value("VIEWER"));
    }

    @Test
    void revokeMemberReturnsRevokedMembership() throws Exception {
        TestClient partner = createPartner();
        TestCompany company = createCompany(partner);
        TestClient member = createMember();
        TestMembership membership = createMembership(company, member, CompanyMemberRole.OPERATOR);

        mockMvc.perform(patch("/companies/" + company.id() + "/members/" + membership.id() + "/revoke")
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.membershipId").value(membership.id()))
                .andExpect(jsonPath("$.clientId").value(member.id()))
                .andExpect(jsonPath("$.status").value("REVOKED"));
    }

    @Test
    void addMemberRejectsInvalidRequest() throws Exception {
        TestClient partner = createPartner();
        TestCompany company = createCompany(partner);

        mockMvc.perform(post("/companies/" + company.id() + "/members")
                        .header("Authorization", bearer(managerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(membershipJson(0L, CompanyMemberRole.OPERATOR)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void associateCannotManageMembers() throws Exception {
        TestClient partner = createPartner();
        TestCompany company = createCompany(partner);
        TestClient member = createMember();
        TestMembership membership = createMembership(company, member, CompanyMemberRole.OPERATOR);
        TestClient secondMember = createMember();
        String token = associateToken();

        mockMvc.perform(post("/companies/" + company.id() + "/members")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(membershipJson(secondMember.id(), CompanyMemberRole.OPERATOR)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/companies/" + company.id() + "/members")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/companies/" + company.id() + "/members/" + membership.id() + "/role")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson(CompanyMemberRole.VIEWER)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/companies/" + company.id() + "/members/" + membership.id() + "/revoke")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonMemberCannotManageMembers() throws Exception {
        TestClient partner = createPartner();
        TestCompany company = createCompany(partner);
        TestClient member = createMember();
        TestMembership membership = createMembership(company, member, CompanyMemberRole.OPERATOR);
        TestClient secondMember = createMember();
        String token = login(member.email(), member.password());

        mockMvc.perform(post("/companies/" + company.id() + "/members")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(membershipJson(secondMember.id(), CompanyMemberRole.OPERATOR)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/companies/" + company.id() + "/members")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/companies/" + company.id() + "/members/" + membership.id() + "/role")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson(CompanyMemberRole.VIEWER)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/companies/" + company.id() + "/members/" + membership.id() + "/revoke")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void partnerCanManageMembers() throws Exception {
        TestClient partner = createPartner();
        TestCompany company = createCompany(partner);
        TestClient member = createMember();
        TestMembership membership = createMembership(company, member, CompanyMemberRole.OPERATOR);
        TestClient secondMember = createMember();
        String token = login(partner.email(), partner.password());

        mockMvc.perform(post("/companies/" + company.id() + "/members")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(membershipJson(secondMember.id(), CompanyMemberRole.OPERATOR)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/companies/" + company.id() + "/members")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/companies/" + company.id() + "/members/" + membership.id() + "/role")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson(CompanyMemberRole.VIEWER)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/companies/" + company.id() + "/members/" + membership.id() + "/revoke")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void managerCanManageMembersOfAnyCompany() throws Exception {
        TestClient partner = createPartner();
        TestCompany company = createCompany(partner);
        TestClient member = createMember();
        TestMembership membership = createMembership(company, member, CompanyMemberRole.OPERATOR);
        TestClient secondMember = createMember();
        String token = managerToken();

        mockMvc.perform(post("/companies/" + company.id() + "/members")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(membershipJson(secondMember.id(), CompanyMemberRole.OPERATOR)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/companies/" + company.id() + "/members")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/companies/" + company.id() + "/members/" + membership.id() + "/role")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson(CompanyMemberRole.VIEWER)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/companies/" + company.id() + "/members/" + membership.id() + "/revoke")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void revokedMemberIsNotListed() throws Exception {
        TestClient partner = createPartner();
        TestCompany company = createCompany(partner);
        TestClient member = createMember();
        TestMembership membership = createMembership(company, member, CompanyMemberRole.OPERATOR);

        mockMvc.perform(patch("/companies/" + company.id() + "/members/" + membership.id() + "/revoke")
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/companies/" + company.id() + "/members")
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.membershipId == " + membership.id() + ")]").doesNotExist());
    }

    // AUXILIARY

    private static String uniqueLegalName() {
        return "Ctrl Legal Name " + COUNTER.incrementAndGet();
    }

    private static String uniqueTradeName() {
        return "Ctrl Trade Name " + COUNTER.incrementAndGet();
    }

    private static String uniqueRegistrationNumber() {
        return "COMP" + String.format("%08d", COUNTER.incrementAndGet()) + "00";
    }

    private String registrationJson(String legalName, String tradeName, String registrationNumber,
                                    Long partnerId, String foundationDate) {
        return """
                {"legalName":"%s","tradeName":"%s","registrationNumber":"%s","partnerId":%d,"foundationDate":"%s"}
                """.formatted(legalName, tradeName, registrationNumber, partnerId, foundationDate);
    }

    private String updateJson(String legalName, String tradeName, Long partnerId, String registrationNumber) {
        return """
                {"legalName":"%s","tradeName":"%s","partnerId":%d,"registrationNumber":"%s"}
                """.formatted(legalName, tradeName, partnerId, registrationNumber);
    }

    private String membershipJson(Long clientId, CompanyMemberRole role) {
        return """
                {"clientId":%d,"role":"%s"}
                """.formatted(clientId, role);
    }

    private String foundationDateJson(String foundationDate) {
        return """
                {"foundationDate":"%s"}
                """.formatted(foundationDate);
    }

    private String roleJson(CompanyMemberRole role) {
        return """
                {"role":"%s"}
                """.formatted(role);
    }

    private ResultActions postRegistration(String json) throws Exception {
        return mockMvc.perform(post("/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions postRegistration(String json, TestClient partner) throws Exception {
        return mockMvc.perform(post("/companies")
                .header("Authorization", bearer(login(partner.email(), partner.password())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private record TestClient(Long id, String email, String password) {
    }

    private record TestMembership(Long id, Long companyId, Long clientId, CompanyMemberRole role) {
    }

    private TestClient createPartner() throws Exception {
        String email = "partner" + COUNTER.incrementAndGet() + "@tbank.test";
        String password = "a-strong-password-1";
        MvcResult result = mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Company Partner","birthDate":"1990-01-01","document":"%s","email":"%s","password":"%s"}
                                """.formatted("4000" + COUNTER.incrementAndGet(), email, password)))
                .andExpect(status().isCreated())
                .andReturn();
        return new TestClient(extractId(result.getResponse().getContentAsString()), email, password);
    }

    private record TestCompany(Long id, String legalName, String tradeName,
                               String registrationNumber, Long partnerId) {
    }

    private TestCompany createCompany(TestClient partner) throws Exception {
        String legalName = uniqueLegalName();
        String tradeName = uniqueTradeName();
        String registrationNumber = uniqueRegistrationNumber();
        MvcResult result = postRegistration(registrationJson(
                legalName, tradeName, registrationNumber, partner.id(), "2023-01-01"), partner)
                .andExpect(status().isCreated())
                .andReturn();
        return new TestCompany(extractId(result.getResponse().getContentAsString()),
                legalName, tradeName, registrationNumber, partner.id());
    }

    private TestClient createMember() throws Exception {
        String email = "member" + COUNTER.incrementAndGet() + "@tbank.test";
        String password = "a-strong-password-2";
        MvcResult result = mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Company Member","birthDate":"1995-01-01","document":"%s","email":"%s","password":"%s"}
                                """.formatted("5000" + COUNTER.incrementAndGet(), email, password)))
                .andExpect(status().isCreated())
                .andReturn();
        return new TestClient(extractId(result.getResponse().getContentAsString()), email, password);
    }

    private TestMembership createMembership(TestCompany company, TestClient member, CompanyMemberRole role) throws Exception {
        MvcResult result = mockMvc.perform(post("/companies/" + company.id() + "/members")
                        .header("Authorization", bearer(managerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(membershipJson(member.id(), role)))
                .andExpect(status().isCreated())
                .andReturn();
        Long membershipId = extractMembershipId(result.getResponse().getContentAsString());
        return new TestMembership(membershipId, company.id(), member.id(), role);
    }

    private TestMembership getMembership(TestCompany company, TestClient member) throws Exception {
        MvcResult result = mockMvc.perform(get("/companies/" + company.id() + "/members")
                        .header("Authorization", bearer(managerToken())))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        Matcher matcher = Pattern.compile("\"membershipId\":(\\d+),\"clientId\":" + member.id() + ",\"clientName\":\"[^\"]+\",\"role\":\"([^\"]+)\",\"status\":\"([^\"]+)\"")
                .matcher(responseBody);
        if (!matcher.find()) {
            throw new AssertionError("Response did not include a membership for company " + company.id());
        }
        Long membershipId = Long.valueOf(matcher.group(1));
        CompanyMemberRole role = CompanyMemberRole.valueOf(matcher.group(2));
        return new TestMembership(membershipId, company.id(), member.id(), role);
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

    private Long extractMembershipId(String body) {
        Matcher matcher = Pattern.compile("\"membershipId\":(\\d+)").matcher(body);
        if (!matcher.find()) {
            throw new AssertionError("Response did not include a membershipId");
        }
        return Long.valueOf(matcher.group(1));
    }
}
