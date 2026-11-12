package com.tuon.tbank.security;

import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.entity.company.Company;
import com.tuon.tbank.enums.company.CompanyStatus;
import com.tuon.tbank.repository.company.CompanyMembershipRepository;
import com.tuon.tbank.repository.company.CompanyRepository;
import com.tuon.tbank.security.auth.CompanyAuthorization;
import com.tuon.tbank.service.person.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class CompanyAuthorizationTests {

    private CompanyRepository companyRepository;

    private CompanyMembershipRepository membershipRepository;

    @Autowired
    private ClientService clientService;

    private CompanyAuthorization companyAuthorization;

    @Autowired
    public CompanyAuthorizationTests(CompanyRepository companyRepository,
                                     CompanyMembershipRepository membershipRepository) {
        this.companyRepository = companyRepository;
        this.membershipRepository = membershipRepository;
        this.companyAuthorization = new CompanyAuthorization(companyRepository, membershipRepository);
    }

    // IS OWNER TESTS

    @Test
    void partnerIsOwnerAcceptsCompanyWithMatchingId() {
        Client partner = registerClient();
        Company company = registerCompany(partner);
        Authentication auth = authentication("CLIENT", partner.getId(), "ROLE_CLIENT");

        assertTrue(companyAuthorization.isOwner(company.getId(), partner.getId(), auth));
    }

    @Test
    void partnerIsOwnerRejectsCompanyWithDifferentId() {
        Client partner = registerClient();
        Company company = registerCompany(partner);
        Authentication auth = authentication("CLIENT", partner.getId(), "ROLE_CLIENT");

        assertFalse(companyAuthorization.isOwner(company.getId() + 1, partner.getId(), auth));
    }

    @Test
    void partnerIsOwnerRejectsNullCompanyId() {
        Client partner = registerClient();
        registerCompany(partner);
        Authentication auth = authentication("CLIENT", partner.getId(), "ROLE_CLIENT");

        assertFalse(companyAuthorization.isOwner(null, partner.getId(), auth));
        assertFalse(companyAuthorization.canReadCompany(null, auth));
    }

    @Test
    void partnerIsOwnerRejectsNullAuthentication() {
        assertFalse(companyAuthorization.isOwner(1L, 1L, null));
        assertFalse(companyAuthorization.canReadCompany(1L, null));
    }

    @Test
    void partnerIsOwnerRejectsNonJwtAuthentication() {
        Authentication auth = UsernamePasswordAuthenticationToken
                .authenticated("1", "password", Arrays.asList(new SimpleGrantedAuthority("ROLE_CLIENT")));

        assertFalse(companyAuthorization.isOwner(1L, 1L, auth));
    }

    @Test
    void partnerIsOwnerRejectsNonClientAuthentication() {
        Client partner = registerClient();
        registerCompany(partner);
        Authentication auth = authentication("EMPLOYEE", partner.getId(), "ROLE_ASSOCIATE");

        assertFalse(companyAuthorization.isOwner(1L, partner.getId(), auth));
    }

    @Test
    void partnerIsOwnerRejectsTokenWithoutUserId() {
        Authentication auth = authentication("CLIENT", null, "ROLE_CLIENT");

        assertFalse(companyAuthorization.isOwner(1L, 1L, auth));
    }

    @Test
    void partnerIsOwnerRejectsClientIdentity() {
        Authentication auth = authentication("CLIENT", 1L, "ROLE_CLIENT");

        assertFalse(companyAuthorization.isOwner(Long.MAX_VALUE, 1L, auth));
    }

    @Test
    void partnerIsOwnerRejectsClientWithoutRole() {
        Authentication auth = authentication("CLIENT", 1L);

        assertFalse(companyAuthorization.isOwner(1L, 1L, auth));
    }

    @Test
    void partnerIsOwnerRejectsClientWithWrongRole() {
        Authentication auth = authentication("CLIENT", 1L, "ROLE_EMPLOYEE");

        assertFalse(companyAuthorization.isOwner(1L, 1L, auth));
    }

    @Test
    void partnerIsOwnerRejectsEmployeeIdentity() {
        Authentication auth = authentication("EMPLOYEE", 1L, "ROLE_ASSOCIATE");

        assertFalse(companyAuthorization.isOwner(1L, 1L, auth));
    }

    // CAN READ TESTS

    @Test
    void canReadCompanyAsOwner(){
        Client owner = registerClient();
        Company company = registerCompany(owner);
        Authentication auth = authentication("CLIENT", owner.getId(), "ROLE_CLIENT");

        assertTrue(companyAuthorization.canReadCompany(company.getId(), auth));
    }

    @Test
    void canReadCompanyAsEmployee(){
        Client owner = registerClient();
        Company company = registerCompany(owner);
        Authentication authAssociate = authentication("EMPLOYEE", owner.getId(), "ROLE_ASSOCIATE");
        Authentication authManager = authentication("EMPLOYEE", owner.getId(), "ROLE_MANAGER");
        Authentication authAdmin = authentication("EMPLOYEE", owner.getId(), "ROLE_ADMIN");

        assertTrue(companyAuthorization.canReadCompany(company.getId(), authAssociate));
        assertTrue(companyAuthorization.canReadCompany(company.getId(), authManager));
        assertTrue(companyAuthorization.canReadCompany(company.getId(), authAdmin));
    }

    @Test
    void canReadCompanyRejectsNonOwnerClient(){
        Client owner = registerClient();
        Company company = registerCompany(owner);
        Client nonOwner = registerClient();
        Authentication auth = authentication("CLIENT", nonOwner.getId(), "ROLE_CLIENT");

        assertFalse(companyAuthorization.canReadCompany(company.getId(), auth));
    }

    // AUXILIARY

    private static final AtomicLong COUNTER = new AtomicLong(9000);

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

    private Client registerClient() {
        return clientService.registerClient("Test Client " + COUNTER.incrementAndGet(),
                LocalDate.of(1990, 1, 1), "8500" + COUNTER.incrementAndGet(),
                "test-client" + COUNTER.incrementAndGet() + "@email.test", "a-strong-password");
    }

    private Company registerCompany(Client partner) {
        return companyRepository.save(new Company(
                null,
                "Test Legal Name " + COUNTER.incrementAndGet(),
                "Test Trade Name " + COUNTER.incrementAndGet(),
                partner,
                "COMP" + String.format("%08d", COUNTER.incrementAndGet()) + "00",
                LocalDate.of(2020, 1, 1),
                LocalDate.now(),
                CompanyStatus.ACTIVE));
    }
}
