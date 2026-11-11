package com.tuon.tbank.service;

import com.tuon.tbank.entity.company.Company;
import com.tuon.tbank.entity.company.CompanyMembership;
import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.enums.company.CompanyMemberRole;
import com.tuon.tbank.enums.company.MembershipStatus;
import com.tuon.tbank.enums.person.EmployeeRole;
import com.tuon.tbank.exception.serviceException.BusinessException;
import com.tuon.tbank.repository.company.CompanyMembershipRepository;
import com.tuon.tbank.repository.company.CompanyRepository;
import com.tuon.tbank.service.company.CompanyMembershipService;
import com.tuon.tbank.service.company.CompanyService;
import com.tuon.tbank.service.person.ClientService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class CompanyServiceTests {

    private static final AtomicLong COUNTER = new AtomicLong(9000);
    private static final String TEST_COMPANY_NAME = "Test Company";
    private static final String TEST_COMPANY_TRADE_NAME = "Test Trade Name";
    private static final LocalDate TEST_COMPANY_FOUNDATION_DATE = LocalDate.of(2020, 1, 1);
    private static final String TEST_REGISTRATION_NUMBER = "0000000000";

    @Autowired
    private ClientService clientService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyMembershipRepository companyMembershipRepository;

    @Autowired
    private CompanyMembershipService companyMembershipService;

    // COMPANY TESTS

    @Test
    void managerRegisterCompanyCreatesValidCompany() {
        authenticateAsManager();

        Company company = registerCompany();
        Company saved = companyRepository.findById(company.getId()).orElseThrow();

        assertAll(
                () -> assertEquals(company.getId(), saved.getId()),
                () -> assertEquals(company.getLegalName(), saved.getLegalName()),
                () -> assertEquals(company.getTradeName(), saved.getTradeName()),
                () -> assertEquals(company.getRegistrationNumber(), saved.getRegistrationNumber()),
                () -> assertEquals(company.getFoundationDate(), saved.getFoundationDate()),
                () -> assertEquals(company.getPartner().getId(), saved.getPartner().getId())
        );
    }

    @Test
    void clientRegisterCompanyCreatesValidCompany() {
        Client owner = registerClient();
        authenticateAsClient(owner.getId());
        Company company = companyService.registerCompany(TEST_COMPANY_NAME + " " + COUNTER.incrementAndGet(),
                TEST_COMPANY_TRADE_NAME + " " + COUNTER.incrementAndGet(),
                TEST_REGISTRATION_NUMBER + " " + COUNTER.incrementAndGet(),
                owner.getId(),
                TEST_COMPANY_FOUNDATION_DATE);
        Company saved = companyRepository.findById(company.getId()).orElseThrow();

        assertAll(
                () -> assertEquals(company.getId(), saved.getId()),
                () -> assertEquals(company.getLegalName(), saved.getLegalName()),
                () -> assertEquals(company.getTradeName(), saved.getTradeName()),
                () -> assertEquals(company.getRegistrationNumber(), saved.getRegistrationNumber()),
                () -> assertEquals(company.getFoundationDate(), saved.getFoundationDate()),
                () -> assertEquals(company.getPartner().getId(), saved.getPartner().getId())
        );
    }

    @Test
    void associateRegisterCompanyDoesNotHavePermissionToRegisterCompany() {
        authenticateAsAssociate();

        assertThrows(AuthorizationDeniedException.class, this::registerCompany);
    }

    @Test
    void registerCompanyRejectsInvalidBusinessData() {
        authenticateAsManager();
        registerClient();
        assertAll(
                () -> assertThrows(BusinessException.class,
                        () -> companyService.registerCompany(null, TEST_COMPANY_TRADE_NAME + " " + COUNTER.incrementAndGet(), TEST_REGISTRATION_NUMBER + " " + COUNTER.incrementAndGet(),
                                registerClient().getId(), TEST_COMPANY_FOUNDATION_DATE)),
                () -> assertThrows(BusinessException.class,
                        () -> companyService.registerCompany(TEST_COMPANY_NAME + " " + COUNTER.incrementAndGet(), null, TEST_REGISTRATION_NUMBER + " " + COUNTER.incrementAndGet(),
                                registerClient().getId(), TEST_COMPANY_FOUNDATION_DATE)),
                () -> assertThrows(BusinessException.class,
                        () -> companyService.registerCompany(TEST_COMPANY_NAME + " " + COUNTER.incrementAndGet(), TEST_COMPANY_TRADE_NAME + " " + COUNTER.incrementAndGet(), null,
                                registerClient().getId(), TEST_COMPANY_FOUNDATION_DATE)),
                () -> assertThrows(BusinessException.class,
                        () -> companyService.registerCompany(TEST_COMPANY_NAME + " " + COUNTER.incrementAndGet(), TEST_COMPANY_TRADE_NAME + " " + COUNTER.incrementAndGet(), TEST_REGISTRATION_NUMBER + " " + COUNTER.incrementAndGet(),
                                null, TEST_COMPANY_FOUNDATION_DATE)),
                () -> assertThrows(BusinessException.class,
                        () -> companyService.registerCompany(TEST_COMPANY_NAME + " " + COUNTER.incrementAndGet(), TEST_COMPANY_TRADE_NAME + " " + COUNTER.incrementAndGet(), TEST_REGISTRATION_NUMBER + " " + COUNTER.incrementAndGet(),
                                registerClient().getId(), null))
        );
    }

    @Test
    void registerCompanyRejectsDuplicatedLegalName() {
        authenticateAsManager();
        Company owner = registerCompany();

        assertThrows(BusinessException.class, () -> companyService.registerCompany(owner.getLegalName(),
                TEST_COMPANY_TRADE_NAME + " " + COUNTER.incrementAndGet(),
                TEST_REGISTRATION_NUMBER + " " + COUNTER.incrementAndGet(),
                registerClient().getId(),
                TEST_COMPANY_FOUNDATION_DATE));
    }

    @Test
    void registerCompanyRejectsDuplicatedRegistrationNumber() {
        authenticateAsManager();
        Company owner = registerCompany();

        assertThrows(BusinessException.class, () -> companyService.registerCompany(TEST_COMPANY_NAME + " " + COUNTER.incrementAndGet(),
                TEST_COMPANY_TRADE_NAME + " " + COUNTER.incrementAndGet(),
                owner.getRegistrationNumber(),
                registerClient().getId(),
                TEST_COMPANY_FOUNDATION_DATE));
    }

    @Test
    void updateCompanyAndFoundationDatePreserveExpectedData() {
        authenticateAsManager();
        Company company = registerCompany();

        String newLegalName = "Updated Legal Name";
        String newTradeName = "Updated Trade Name";
        String newRegistrationNumber = String.format("NEW%09d01", COUNTER.incrementAndGet());
        LocalDate newFoundationDate = LocalDate.of(2021, 1, 1);

        companyService.updateCompany(company.getId(), newLegalName, newTradeName, company.getPartner().getId(), newRegistrationNumber);
        companyService.updateFoundationDate(company.getId(), newFoundationDate);

        Company updatedCompany = companyRepository.findById(company.getId()).orElseThrow();

        assertAll(
                () -> assertEquals(newLegalName, updatedCompany.getLegalName()),
                () -> assertEquals(newTradeName, updatedCompany.getTradeName()),
                () -> assertEquals(newFoundationDate, updatedCompany.getFoundationDate()),
                () -> assertEquals(newRegistrationNumber, updatedCompany.getRegistrationNumber()),
                () -> assertEquals(company.getPartner().getId(), updatedCompany.getPartner().getId())
        );
    }

    @Test
    void updateCompanyRejectsInvalidData() {
        authenticateAsManager();
        Company company = registerCompany();

        assertAll(
                () -> assertThrows(BusinessException.class,
                        () -> companyService.updateCompany(company.getId(), null, company.getTradeName(), company.getPartner().getId(), company.getRegistrationNumber())),
                () -> assertThrows(BusinessException.class,
                        () -> companyService.updateCompany(company.getId(), company.getLegalName(), null, company.getPartner().getId(), company.getRegistrationNumber())),
                () -> assertThrows(BusinessException.class,
                        () -> companyService.updateCompany(company.getId(), company.getLegalName(), company.getTradeName(), null, company.getRegistrationNumber())),
                () -> assertThrows(BusinessException.class,
                        () -> companyService.updateCompany(company.getId(), company.getLegalName(), company.getTradeName(), company.getPartner().getId(), null)),
                () -> assertThrows(BusinessException.class, () -> companyService.updateFoundationDate(company.getId(), null))
        );
    }

    // COMPANY MEMBERSHIP TESTS

    @Test
    void addMemberCreatesValidMembership() {
        authenticateAsManager();
        Company company = registerCompany();
        Client client = registerClient();

        CompanyMembership membership = registerCompanyMembershipOperator(company.getId(), client.getId());
        CompanyMembership saved = companyMembershipRepository.findById(membership.getId()).orElseThrow();

        assertAll(
                () -> assertEquals(membership.getId(), saved.getId()),
                () -> assertEquals(membership.getCompany().getId(), saved.getCompany().getId()),
                () -> assertEquals(membership.getClient().getId(), saved.getClient().getId()),
                () -> assertEquals(membership.getRole(), saved.getRole()),
                () -> assertEquals(membership.getStatus(), saved.getStatus())
        );
    }

    @Test
    void addInvalidMemberThrowsBusinessException() {
        authenticateAsManager();
        Company company = registerCompany();
        Client client = registerClient();

        assertAll(
                () -> assertThrows(BusinessException.class,
                        () -> companyMembershipService.addMember(company.getId(), client.getId(), null)),
                () -> assertThrows(BusinessException.class,
                        () -> companyMembershipService.addMember(company.getId(), null, CompanyMemberRole.OPERATOR)),
                () -> assertThrows(BusinessException.class,
                        () -> companyMembershipService.addMember(null, client.getId(), CompanyMemberRole.OPERATOR))
        );
    }

    @Test
    void listActiveMembershipsByCompanyReturnsCorrectMemberships() {
        authenticateAsManager();
        Company company = registerCompany();
        Client client1 = registerClient();
        Client client2 = registerClient();
        Client client3 = registerClient();

        CompanyMembership membership1 = registerCompanyMembershipOperator(company.getId(), client1.getId());
        CompanyMembership membership2 = registerCompanyMembershipViewer(company.getId(), client2.getId());
        CompanyMembership membership3 = registerCompanyMembershipViewer(company.getId(), client3.getId());

        CompanyMembership revokedMembership = companyMembershipService.revoke(company.getId(), membership3.getId());

        var memberships = companyMembershipService.listMembers(company.getId());

        assertAll(
                () -> assertTrue(memberships.contains(membership1)),
                () -> assertTrue(memberships.contains(membership2)),
                () -> assertFalse(memberships.contains(revokedMembership))
        );
    }

    @Test
    void changeRoleOfMembershipUpdatesRole() {
        authenticateAsManager();
        Company company = registerCompany();
        Client client = registerClient();

        CompanyMembership membership = registerCompanyMembershipOperator(company.getId(), client.getId());

        companyMembershipService.changeRole(company.getId(), membership.getId(), CompanyMemberRole.VIEWER);

        CompanyMembership updatedMembership = companyMembershipRepository.findById(membership.getId()).orElseThrow();

        assertEquals(CompanyMemberRole.VIEWER, updatedMembership.getRole());
    }

    @Test
    void revokeRoleOfMembershipChangesStatusToRevoked() {
        authenticateAsManager();
        Company company = registerCompany();
        Client client = registerClient();

        CompanyMembership membership = registerCompanyMembershipOperator(company.getId(), client.getId());

        companyMembershipService.revoke(company.getId(), membership.getId());

        CompanyMembership updatedMembership = companyMembershipRepository.findById(membership.getId()).orElseThrow();

        assertEquals(MembershipStatus.REVOKED, updatedMembership.getStatus());
    }

    @Test
    void getMembershipReturnsCorrectMembership() {
        authenticateAsManager();

        Company company = registerCompany();
        Client operator1 = registerClient();
        Client operator2 = registerClient();
        Client viewer1 = registerClient();
        Client viewer2 = registerClient();

        CompanyMembership membershipOperator1 = registerCompanyMembershipOperator(company.getId(), operator1.getId());
        CompanyMembership membershipOperator2 = registerCompanyMembershipOperator(company.getId(), operator2.getId());
        CompanyMembership membershipViewer1 = registerCompanyMembershipViewer(company.getId(), viewer1.getId());
        CompanyMembership membershipViewer2 = registerCompanyMembershipViewer(company.getId(), viewer2.getId());

        Optional<CompanyMembership> retrievedMembershipByCompanyAndId = companyMembershipService.getMembershipByCompanyIdAndClientId(company.getId(), operator1.getId());
        List<CompanyMembership> retrieveMembershipByCompanyIdAndStatus = companyMembershipService.getByCompanyIdAndStatus(company.getId(), MembershipStatus.ACTIVE);
        Optional<CompanyMembership> retrievedMembershipById = companyMembershipService.getMembershipById(membershipOperator1.getId(), company.getId());

        assertAll(
                () -> assertTrue(retrievedMembershipByCompanyAndId.isPresent()),
                () -> assertEquals(membershipOperator1.getId(), retrievedMembershipByCompanyAndId.get().getId()),
                () -> assertTrue(retrieveMembershipByCompanyIdAndStatus.contains(membershipOperator1)),
                () -> assertTrue(retrieveMembershipByCompanyIdAndStatus.contains(membershipOperator2)),
                () -> assertTrue(retrieveMembershipByCompanyIdAndStatus.contains(membershipViewer1)),
                () -> assertTrue(retrieveMembershipByCompanyIdAndStatus.contains(membershipViewer2)),
                () -> assertTrue(retrievedMembershipById.isPresent()),
                () -> assertEquals(membershipOperator1.getId(), retrievedMembershipById.get().getId())
        );
    }

    @Test
    void deleteCompanyRemovesCompanyAndMemberships() {
        authenticateAsManager();
        Company company = registerCompany();
        Client client = registerClient();
        CompanyMembership membership = registerCompanyMembershipOperator(company.getId(), client.getId());

        companyService.deleteCompany(company.getId());

        Company deleted = companyRepository.findById(company.getId()).orElseThrow();

        assertTrue(deleted.isDeleted());
        assertTrue(companyMembershipRepository.existsById(membership.getId()));
    }

    // AUXILIARY

    private Client registerClient() {
        return clientService.registerClient("Test Client " + COUNTER.incrementAndGet(),
                LocalDate.of(1990, 1, 1), "8500" + COUNTER.incrementAndGet(),
                "test-client" + COUNTER.incrementAndGet() + "@email.test", "a-strong-password");
    }

    private Company registerCompany() {
        registerClient();
        return companyService.registerCompany(TEST_COMPANY_NAME + " " + COUNTER.incrementAndGet(),
                TEST_COMPANY_TRADE_NAME + " " + COUNTER.incrementAndGet(),
                TEST_REGISTRATION_NUMBER + " " + COUNTER.incrementAndGet(),
                registerClient().getId(),
                TEST_COMPANY_FOUNDATION_DATE);
    }

    private CompanyMembership registerCompanyMembershipOperator(Long companyId, Long clientId) {
        return companyMembershipService.addMember(companyId, clientId, CompanyMemberRole.OPERATOR);
    }

    private CompanyMembership registerCompanyMembershipViewer(Long companyId, Long clientId) {
        return companyMembershipService.addMember(companyId, clientId, CompanyMemberRole.VIEWER);
    }

    private void authenticateAsClient(Long clientId) {
        Jwt token = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("identity_type", "CLIENT")
                .claim("user_id", clientId)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(
                        token,
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
                ));
    }

    private void authenticateAsEmployee(long userId, EmployeeRole role) {
        Jwt token = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("identity_type", "EMPLOYEE")
                .claim("user_id", userId)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(token,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))));
    }

    private void authenticateAsManager() {
        authenticateAsEmployee(1L, EmployeeRole.MANAGER);
    }

    private void authenticateAsAssociate() {
        authenticateAsEmployee(2L, EmployeeRole.ASSOCIATE);
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}
