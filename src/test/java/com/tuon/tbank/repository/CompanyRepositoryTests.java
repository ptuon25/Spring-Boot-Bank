package com.tuon.tbank.repository;

import com.tuon.tbank.entity.company.Company;
import com.tuon.tbank.entity.company.CompanyMembership;
import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.enums.company.CompanyMemberRole;
import com.tuon.tbank.enums.company.CompanyStatus;
import com.tuon.tbank.enums.company.MembershipStatus;
import com.tuon.tbank.enums.person.ClientStatus;
import com.tuon.tbank.repository.company.CompanyMembershipRepository;
import com.tuon.tbank.repository.company.CompanyRepository;
import com.tuon.tbank.repository.person.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class CompanyRepositoryTests {

    private static final AtomicLong COUNTER = new AtomicLong(8500);
    private static final LocalDate FOUNDATION_DATE = LocalDate.of(2020, 1, 1);
    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);


    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyMembershipRepository companyMembershipRepository;

    @Autowired
    private ClientRepository clientRepository;

    // COMPANY TESTS

    @Test
    void findCompanyReturnsTrueResults() {
        Client owner1 = saveClient();
        Client owner2 = saveClient();
        Client owner3 = saveClient();
        Client owner4 = saveClient();
        Client owner5 = saveClient();
        Client owner6 = saveClient();

        Company company1 = saveCompany(owner1);
        Company company2 = saveCompany(owner2);
        Company company3 = saveCompany(owner3);
        Company company4 = saveCompany(owner4);
        Company company5 = saveCompany(owner5);
        Company company6 = saveCompany(owner6);

        Client deletedOwner1 = saveDeletedClient();
        Client deletedOwner2 = saveDeletedClient();
        Client deletedOwner3 = saveDeletedClient();

        Company deletedCompany1 = saveDeletedCompany(deletedOwner1);
        Company deletedCompany2 = saveDeletedCompany(deletedOwner2);
        Company deletedCompany3 = saveDeletedCompany(deletedOwner3);

        List<Company> findAll = companyRepository.findAll();
        List<Company> findByDeletedTrue = companyRepository.findByDeletedTrue();
        Optional<Company> findByIdAndDeletedFalse = companyRepository.findByIdAndDeletedFalse(company1.getId());
        Optional<Company> findByLegalNameAndDeletedFalse = companyRepository.findByLegalNameAndDeletedFalse(company2.getLegalName());
        List<Company> findByTradeNameAndDeletedFalse = companyRepository.findByTradeNameAndDeletedFalse(company3.getTradeName());
        List<Company> findByPartnerAndDeletedFalse = companyRepository.findByPartnerAndDeletedFalse(owner4);
        Optional<Company> findByRegistrationNumberAndDeletedFalse = companyRepository.findByRegistrationNumberAndDeletedFalse(company5.getRegistrationNumber());
        List<Company> findByStatusAndDeletedFalse = companyRepository.findByStatusAndDeletedFalseOrderByLegalNameAsc(CompanyStatus.ACTIVE);
        List<Company> findByFoundationDateBetweenAndDeletedFalse = companyRepository.findByFoundationDateBetweenAndDeletedFalse(FOUNDATION_DATE.minusDays(1), FOUNDATION_DATE.plusDays(1));
        List<Company> findByAffiliationDateBetweenAndDeletedFalse = companyRepository.findByAffiliationDateBetweenAndDeletedFalse(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        List<Company> findByPartnerIdAndDeletedFalse = companyRepository.findByPartnerIdAndDeletedFalse(owner6.getId());

        assertAll(
                () -> assertEquals(9, findAll.size()),
                () -> assertEquals(3, findByDeletedTrue.size()),
                () -> assertTrue(findByIdAndDeletedFalse.isPresent()),
                () -> assertTrue(findByLegalNameAndDeletedFalse.isPresent()),
                () -> assertEquals(1, findByTradeNameAndDeletedFalse.size()),
                () -> assertEquals(1, findByPartnerAndDeletedFalse.size()),
                () -> assertTrue(findByRegistrationNumberAndDeletedFalse.isPresent()),
                () -> assertEquals(6, findByStatusAndDeletedFalse.size()),
                () -> assertEquals(6, findByFoundationDateBetweenAndDeletedFalse.size()),
                () -> assertEquals(6, findByAffiliationDateBetweenAndDeletedFalse.size()),
                () -> assertEquals(1, findByPartnerIdAndDeletedFalse.size())
        );
    }

    @Test
    void findCompanyReturnsFalseResults() {
        Client nonExistentClient = saveDeletedClient();
        Company nonExistentCompany = saveDeletedCompany(nonExistentClient);

        Optional<Company> nonExistentCompanyId = companyRepository.findByIdAndDeletedFalse(9999L);
        Optional<Company> nonExistentDeletedCompanyId = companyRepository.findByIdAndDeletedFalse(nonExistentCompany.getId());
        Optional<Company> nonExistentCompanyLegalName = companyRepository.findByLegalNameAndDeletedFalse("Non Existent Legal Name");
        List<Company> nonExistentCompanyTradeName = companyRepository.findByTradeNameAndDeletedFalse("Non Existent Trade Name");
        List<Company> nonExistentCompanyPartner = companyRepository.findByPartnerAndDeletedFalse(nonExistentClient);
        Optional<Company> nonExistentRegistrationNumber = companyRepository.findByRegistrationNumberAndDeletedFalse("Non Existent Registration Number");
        List<Company> nonExistentCompanyStatus = companyRepository.findByStatusAndDeletedFalseOrderByLegalNameAsc(CompanyStatus.INACTIVE);
        List<Company> nonExistentCompanyFoundationDate = companyRepository.findByFoundationDateBetweenAndDeletedFalse(FOUNDATION_DATE.minusDays(10), FOUNDATION_DATE.minusDays(5));
        List<Company> nonExistentCompanyAffiliationDate = companyRepository.findByAffiliationDateBetweenAndDeletedFalse(LocalDate.now().minusDays(10), LocalDate.now().minusDays(5));
        List<Company> nonExistentCompanyPartnerId = companyRepository.findByPartnerIdAndDeletedFalse(nonExistentClient.getId());

        assertAll(
                () -> assertTrue(nonExistentCompanyId.isEmpty()),
                () -> assertTrue(nonExistentDeletedCompanyId.isEmpty()),
                () -> assertTrue(nonExistentCompanyLegalName.isEmpty()),
                () -> assertEquals(0, nonExistentCompanyTradeName.size()),
                () -> assertEquals(0, nonExistentCompanyPartner.size()),
                () -> assertTrue(nonExistentRegistrationNumber.isEmpty()),
                () -> assertEquals(0, nonExistentCompanyStatus.size()),
                () -> assertEquals(0, nonExistentCompanyFoundationDate.size()),
                () -> assertEquals(0, nonExistentCompanyAffiliationDate.size()),
                () -> assertEquals(0, nonExistentCompanyPartnerId.size())
        );
    }

    @Test
    void deletedCompanyDoesNotReturnResults() {
        Client deletedOwner1 = saveDeletedClient();
        Client deletedOwner2 = saveDeletedClient();
        Client deletedOwner3 = saveDeletedClient();

        Company deletedCompany1 = saveDeletedCompany(deletedOwner1);
        Company deletedCompany2 = saveDeletedCompany(deletedOwner2);
        Company deletedCompany3 = saveDeletedCompany(deletedOwner3);

        Optional<Company> findByIdAndDeletedFalse = companyRepository.findByIdAndDeletedFalse(deletedCompany1.getId());
        Optional<Company> findByLegalNameAndDeletedFalse = companyRepository.findByLegalNameAndDeletedFalse("Legal Name Test");
        List<Company> findByTradeNameAndDeletedFalse = companyRepository.findByTradeNameAndDeletedFalse("Trade Name Test");
        List<Company> findByPartnerAndDeletedFalse = companyRepository.findByPartnerAndDeletedFalse(deletedOwner2);
        Optional<Company> findByRegistrationNumberAndDeletedFalse = companyRepository.findByRegistrationNumberAndDeletedFalse("01" + COUNTER.get());
        List<Company> findByStatusAndDeletedFalse = companyRepository.findByStatusAndDeletedFalseOrderByLegalNameAsc(CompanyStatus.ACTIVE);
        List<Company> findByFoundationDateBetweenAndDeletedFalse = companyRepository.findByFoundationDateBetweenAndDeletedFalse(FOUNDATION_DATE.minusDays(1), FOUNDATION_DATE.plusDays(1));
        List<Company> findByAffiliationDateBetweenAndDeletedFalse = companyRepository.findByAffiliationDateBetweenAndDeletedFalse(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        List<Company> findByPartnerIdAndDeletedFalse = companyRepository.findByPartnerIdAndDeletedFalse(deletedOwner3.getId());

        assertAll(
                () -> assertTrue(findByIdAndDeletedFalse.isEmpty()),
                () -> assertTrue(findByLegalNameAndDeletedFalse.isEmpty()),
                () -> assertEquals(0, findByTradeNameAndDeletedFalse.size()),
                () -> assertEquals(0, findByPartnerAndDeletedFalse.size()),
                () -> assertTrue(findByRegistrationNumberAndDeletedFalse.isEmpty()),
                () -> assertEquals(0, findByStatusAndDeletedFalse.size()),
                () -> assertEquals(0, findByFoundationDateBetweenAndDeletedFalse.size()),
                () -> assertEquals(0, findByAffiliationDateBetweenAndDeletedFalse.size()),
                () -> assertEquals(0, findByPartnerIdAndDeletedFalse.size())
        );
    }

    @Test
    void existsByRegistrationNumber() {
        Client owner1 = saveClient();
        Company company1 = saveCompany(owner1);
        Client owner2 = saveDeletedClient();
        Company company2 = saveDeletedCompany(owner2);

        boolean existsByRegistrationNumber = companyRepository.existsByRegistrationNumber(company1.getRegistrationNumber());
        boolean existsByRegistrationNumberDeleted = companyRepository.existsByRegistrationNumber(company2.getRegistrationNumber());

        assertAll(
                () -> assertTrue(existsByRegistrationNumber),
                () -> assertTrue(existsByRegistrationNumberDeleted)
        );
    }

    @Test
    void existsByLegalName() {
        Client owner1 = saveClient();
        Company company1 = saveCompany(owner1);
        Client owner2 = saveDeletedClient();
        Company company2 = saveDeletedCompany(owner2);

        boolean existsByLegalName = companyRepository.existsByLegalName(company1.getLegalName());
        boolean existsByLegalNameDeleted = companyRepository.existsByLegalName(company2.getLegalName());

        assertAll(
                () -> assertTrue(existsByLegalName),
                () -> assertTrue(existsByLegalNameDeleted)
        );
    }

    @Test
    void existsByLegalNameAndIdNot() {
        Client owner = saveClient();
        Company company = saveCompany(owner);

        Client otherOwner = saveClient();
        Company otherCompany = saveCompany(otherOwner);

        Client deletedOwner = saveDeletedClient();
        Company deletedCompany = saveDeletedCompany(deletedOwner);

        Client deletedOtherOwner = saveDeletedClient();
        Company deletedOtherCompany = saveDeletedCompany(deletedOtherOwner);

        boolean existsNonDeletedByLegalNameAndIdNot = companyRepository.existsByLegalNameAndIdNot(company.getLegalName(), company.getId());
        boolean existsDeletedByLegalNameAndIdNot = companyRepository.existsByLegalNameAndIdNot(deletedCompany.getLegalName(), deletedCompany.getId());
        boolean existsNonDeletedByLegalNameAndIdNotOther = companyRepository.existsByLegalNameAndIdNot(company.getLegalName(), otherCompany.getId());
        boolean existsDeletedByLegalNameAndIdNotOther = companyRepository.existsByLegalNameAndIdNot(deletedCompany.getLegalName(), deletedOtherCompany.getId());

        assertAll(
                () -> assertFalse(existsNonDeletedByLegalNameAndIdNot),
                () -> assertFalse(existsDeletedByLegalNameAndIdNot),
                () -> assertTrue(existsNonDeletedByLegalNameAndIdNotOther),
                () -> assertTrue(existsDeletedByLegalNameAndIdNotOther)
        );
    }

    @Test
    void existsByIdAndPartnerIdAndDeletedFalse() {
        Client owner = saveClient();
        Company company = saveCompany(owner);

        Client otherOwner = saveClient();
        Company otherCompany = saveCompany(otherOwner);

        Client deletedOwner = saveDeletedClient();
        Company deletedCompany = saveDeletedCompany(deletedOwner);

        Client deletedOtherOwner = saveDeletedClient();
        Company deletedOtherCompany = saveDeletedCompany(deletedOtherOwner);

        boolean existsNonDeletedByIdAndPartnerId = companyRepository.existsByIdAndPartnerIdAndDeletedFalse(company.getId(), owner.getId());
        boolean existsDeletedByIdAndPartnerId = companyRepository.existsByIdAndPartnerIdAndDeletedFalse(deletedCompany.getId(), deletedOwner.getId());
        boolean existsNonDeletedByIdAndPartnerIdOther = companyRepository.existsByIdAndPartnerIdAndDeletedFalse(company.getId(), otherOwner.getId());
        boolean existsDeletedByIdAndPartnerIdOther = companyRepository.existsByIdAndPartnerIdAndDeletedFalse(deletedCompany.getId(), deletedOtherOwner.getId());

        assertAll(
                () -> assertTrue(existsNonDeletedByIdAndPartnerId),
                () -> assertFalse(existsDeletedByIdAndPartnerId),
                () -> assertFalse(existsNonDeletedByIdAndPartnerIdOther),
                () -> assertFalse(existsDeletedByIdAndPartnerIdOther)
        );
    }

    // COMPANY MEMBERSHIP TESTS

    @Test
    void findCompanyMembershipReturnsTrueResults() {
        Client client1 = saveClient();
        Client client2 = saveClient();
        Client client3 = saveClient();

        Company company1 = saveCompany(client1);
        Company company2 = saveCompany(client2);

        CompanyMembership membership1 = saveCompanyMembershipViewer(company1, client1, CompanyMemberRole.VIEWER);
        CompanyMembership membership2 = saveCompanyMembershipOperator(company1, client2, CompanyMemberRole.OPERATOR);
        CompanyMembership membership3 = saveCompanyMembershipViewer(company2, client3, CompanyMemberRole.VIEWER);

        Optional<CompanyMembership> findByCompanyIdAndClientId = companyMembershipRepository.findByCompanyIdAndClientId(company1.getId(), client1.getId());
        List<CompanyMembership> findByCompanyIdAndStatus = companyMembershipRepository.findByCompanyIdAndStatus(company1.getId(), MembershipStatus.ACTIVE);
        Optional<CompanyMembership> findByIdAndCompanyId = companyMembershipRepository.findByIdAndCompanyId(membership2.getId(), company1.getId());
        boolean existsByCompanyIdAndClientIdAndStatusAndRoleIn = companyMembershipRepository.existsByCompanyIdAndClientIdAndStatusAndRoleIn(company1.getId(), client2.getId(), MembershipStatus.ACTIVE, List.of(CompanyMemberRole.OPERATOR));
        boolean existsByIdAndClientIdAndStatus = companyMembershipRepository.existsByIdAndClientIdAndStatus(membership3.getId(), client3.getId(), MembershipStatus.ACTIVE);
        boolean existsByCompanyIdAndClientId = companyMembershipRepository.existsByCompanyIdAndClientId(company2.getId(), client3.getId());

        assertAll(
                () -> assertTrue(findByCompanyIdAndClientId.isPresent()),
                () -> assertEquals(2, findByCompanyIdAndStatus.size()),
                () -> assertTrue(findByIdAndCompanyId.isPresent()),
                () -> assertTrue(existsByCompanyIdAndClientIdAndStatusAndRoleIn),
                () -> assertTrue(existsByIdAndClientIdAndStatus),
                () -> assertTrue(existsByCompanyIdAndClientId)
        );
    }

    @Test
    void findCompanyMembershipReturnsFalseResults() {
        Client client1 = saveClient();
        Client client2 = saveClient();

        Company company1 = saveCompany(client1);
        Company company2 = saveCompany(client2);

        Optional<CompanyMembership> findByCompanyIdAndClientId = companyMembershipRepository.findByCompanyIdAndClientId(company1.getId(), client2.getId());
        List<CompanyMembership> findByCompanyIdAndStatus = companyMembershipRepository.findByCompanyIdAndStatus(company1.getId(), MembershipStatus.INACTIVE);
        Optional<CompanyMembership> findByIdAndCompanyId = companyMembershipRepository.findByIdAndCompanyId(9999L, company1.getId());
        boolean existsByCompanyIdAndClientIdAndStatusAndRoleIn = companyMembershipRepository.existsByCompanyIdAndClientIdAndStatusAndRoleIn(company1.getId(), client2.getId(), MembershipStatus.ACTIVE, List.of(CompanyMemberRole.OPERATOR));
        boolean existsByIdAndClientIdAndStatus = companyMembershipRepository.existsByIdAndClientIdAndStatus(9999L, client2.getId(), MembershipStatus.ACTIVE);
        boolean existsByCompanyIdAndClientId = companyMembershipRepository.existsByCompanyIdAndClientId(company2.getId(), client1.getId());

        assertAll(
                () -> assertTrue(findByCompanyIdAndClientId.isEmpty()),
                () -> assertEquals(0, findByCompanyIdAndStatus.size()),
                () -> assertTrue(findByIdAndCompanyId.isEmpty()),
                () -> assertFalse(existsByCompanyIdAndClientIdAndStatusAndRoleIn),
                () -> assertFalse(existsByIdAndClientIdAndStatus),
                () -> assertFalse(existsByCompanyIdAndClientId)
        );
    }

    @Test
    void revokedCompanyMembershipDoesNotReturnResults() {
        Client client1 = saveClient();
        Client client2 = saveClient();

        Company company1 = saveCompany(client1);
        Company company2 = saveCompany(client2);

        CompanyMembership revokedMembership1 = saveRevokedCompanyMembership(company1, client1, CompanyMemberRole.VIEWER);
        CompanyMembership revokedMembership2 = saveRevokedCompanyMembership(company2, client2, CompanyMemberRole.OPERATOR);

        Optional<CompanyMembership> findByCompanyIdAndClientId = companyMembershipRepository.findByCompanyIdAndClientId(company1.getId(), client1.getId());
        List<CompanyMembership> findByCompanyIdAndStatus = companyMembershipRepository.findByCompanyIdAndStatus(company2.getId(), MembershipStatus.REVOKED);
        Optional<CompanyMembership> findByIdAndCompanyId = companyMembershipRepository.findByIdAndCompanyId(revokedMembership2.getId(), company2.getId());
        boolean existsByCompanyIdAndClientIdAndStatusAndRoleIn = companyMembershipRepository.existsByCompanyIdAndClientIdAndStatusAndRoleIn(company1.getId(), client1.getId(), MembershipStatus.REVOKED, List.of(CompanyMemberRole.VIEWER));
        boolean existsByIdAndClientIdAndStatus = companyMembershipRepository.existsByIdAndClientIdAndStatus(revokedMembership1.getId(), client1.getId(), MembershipStatus.REVOKED);
        boolean existsByCompanyIdAndClientId = companyMembershipRepository.existsByCompanyIdAndClientId(company2.getId(), client2.getId());

        assertAll(
                () -> assertTrue(findByCompanyIdAndClientId.isPresent()),
                () -> assertEquals(1, findByCompanyIdAndStatus.size()),
                () -> assertTrue(findByIdAndCompanyId.isPresent()),
                () -> assertTrue(existsByCompanyIdAndClientIdAndStatusAndRoleIn),
                () -> assertTrue(existsByIdAndClientIdAndStatus),
                () -> assertTrue(existsByCompanyIdAndClientId)
        );
    }

    @Test
    void existsByCompanyIdAndClientIdAndStatusAndRoleIn() {
        Client client1 = saveClient();
        Client client2 = saveClient();

        Company company1 = saveCompany(client1);
        Company company2 = saveCompany(client2);

        CompanyMembership membership1 = saveCompanyMembershipViewer(company1, client1, CompanyMemberRole.VIEWER);
        CompanyMembership membership2 = saveCompanyMembershipOperator(company2, client2, CompanyMemberRole.OPERATOR);

        boolean existsByCompanyIdAndClientIdAndStatusAndRoleIn1 = companyMembershipRepository.existsByCompanyIdAndClientIdAndStatusAndRoleIn(company1.getId(), client1.getId(), MembershipStatus.ACTIVE, List.of(CompanyMemberRole.VIEWER));
        boolean existsByCompanyIdAndClientIdAndStatusAndRoleIn2 = companyMembershipRepository.existsByCompanyIdAndClientIdAndStatusAndRoleIn(company2.getId(), client2.getId(), MembershipStatus.ACTIVE, List.of(CompanyMemberRole.OPERATOR));

        assertAll(
                () -> assertTrue(existsByCompanyIdAndClientIdAndStatusAndRoleIn1),
                () -> assertTrue(existsByCompanyIdAndClientIdAndStatusAndRoleIn2)
        );
    }

    @Test
    void existsByIdAndClientIdAndStatus() {
        Client client1 = saveClient();
        Client client2 = saveClient();

        Company company1 = saveCompany(client1);
        Company company2 = saveCompany(client2);

        CompanyMembership membership1 = saveCompanyMembershipViewer(company1, client1, CompanyMemberRole.VIEWER);
        CompanyMembership membership2 = saveCompanyMembershipOperator(company2, client2, CompanyMemberRole.OPERATOR);

        boolean existsByIdAndClientIdAndStatus1 = companyMembershipRepository.existsByIdAndClientIdAndStatus(membership1.getId(), client1.getId(), MembershipStatus.ACTIVE);
        boolean existsByIdAndClientIdAndStatus2 = companyMembershipRepository.existsByIdAndClientIdAndStatus(membership2.getId(), client2.getId(), MembershipStatus.ACTIVE);
        boolean existsByIdAndClientIdAndStatus3 = companyMembershipRepository.existsByIdAndClientIdAndStatus(membership1.getId(), client2.getId(), MembershipStatus.ACTIVE);
        boolean existsByIdAndClientIdAndStatus4 = companyMembershipRepository.existsByIdAndClientIdAndStatus(membership2.getId(), client1.getId(), MembershipStatus.ACTIVE);

        assertAll(
                () -> assertTrue(existsByIdAndClientIdAndStatus1),
                () -> assertTrue(existsByIdAndClientIdAndStatus2),
                () -> assertFalse(existsByIdAndClientIdAndStatus3),
                () -> assertFalse(existsByIdAndClientIdAndStatus4)
        );
    }

    @Test
    void existsByCompanyIdAndClientId() {
        Client client1 = saveClient();
        Client client2 = saveClient();

        Company company1 = saveCompany(client1);
        Company company2 = saveCompany(client2);

        CompanyMembership membership1 = saveCompanyMembershipViewer(company1, client1, CompanyMemberRole.VIEWER);
        CompanyMembership membership2 = saveCompanyMembershipOperator(company2, client2, CompanyMemberRole.OPERATOR);

        boolean existsByCompanyIdAndClientId1 = companyMembershipRepository.existsByCompanyIdAndClientId(company1.getId(), client1.getId());
        boolean existsByCompanyIdAndClientId2 = companyMembershipRepository.existsByCompanyIdAndClientId(company2.getId(), client2.getId());
        boolean existsByCompanyIdAndClientId3 = companyMembershipRepository.existsByCompanyIdAndClientId(company1.getId(), client2.getId());
        boolean existsByCompanyIdAndClientId4 = companyMembershipRepository.existsByCompanyIdAndClientId(company2.getId(), client1.getId());

        assertAll(
                () -> assertTrue(existsByCompanyIdAndClientId1),
                () -> assertTrue(existsByCompanyIdAndClientId2),
                () -> assertFalse(existsByCompanyIdAndClientId3),
                () -> assertFalse(existsByCompanyIdAndClientId4)
        );


    }

    @Test
    void existsByIdAndClientIdAndStatusAndRoleIn() {
        Client client1 = saveClient();
        Client client2 = saveClient();

        Company company1 = saveCompany(client1);
        Company company2 = saveCompany(client2);

        CompanyMembership membership1 = saveCompanyMembershipViewer(company1, client1, CompanyMemberRole.VIEWER);
        CompanyMembership membership2 = saveCompanyMembershipOperator(company2, client2, CompanyMemberRole.OPERATOR);

        boolean existsByIdAndClientIdAndStatusAndRoleIn1 = companyMembershipRepository.existsByIdAndClientIdAndStatusAndRoleIn(membership1.getId(), client1.getId(), MembershipStatus.ACTIVE, List.of(CompanyMemberRole.VIEWER));
        boolean existsByIdAndClientIdAndStatusAndRoleIn2 = companyMembershipRepository.existsByIdAndClientIdAndStatusAndRoleIn(membership2.getId(), client2.getId(), MembershipStatus.ACTIVE, List.of(CompanyMemberRole.OPERATOR));
        boolean existsByIdAndClientIdAndStatusAndRoleIn3 = companyMembershipRepository.existsByIdAndClientIdAndStatusAndRoleIn(membership1.getId(), client2.getId(), MembershipStatus.ACTIVE, List.of(CompanyMemberRole.VIEWER));
        boolean existsByIdAndClientIdAndStatusAndRoleIn4 = companyMembershipRepository.existsByIdAndClientIdAndStatusAndRoleIn(membership2.getId(), client1.getId(), MembershipStatus.ACTIVE, List.of(CompanyMemberRole.OPERATOR));

        assertAll(
                () -> assertTrue(existsByIdAndClientIdAndStatusAndRoleIn1),
                () -> assertTrue(existsByIdAndClientIdAndStatusAndRoleIn2),
                () -> assertFalse(existsByIdAndClientIdAndStatusAndRoleIn3),
                () -> assertFalse(existsByIdAndClientIdAndStatusAndRoleIn4)
        );
    }

    // AUXILIARY

    private Client buildClient() {
        long n = COUNTER.incrementAndGet();
        return new Client("Test Client " + n, BIRTH_DATE, "00" + n, "test-client" + n + "@tbank.com",
                "a-strong-password", LocalDate.now(), ClientStatus.ACTIVE);
    }

    private Company buildCompany(Client client) {
        long n = COUNTER.incrementAndGet();
        return new Company(null, "Legal Name Test " + n, "Trade Name Test " + n, client
                , "01" + n, FOUNDATION_DATE, LocalDate.now(), CompanyStatus.ACTIVE);
    }

    private CompanyMembership buildCompanyMembershipViewer(Company company, Client client) {
        return new CompanyMembership(company, client, CompanyMemberRole.VIEWER, MembershipStatus.ACTIVE);
    }

    private CompanyMembership buildCompanyMembershipOperator(Company company, Client client) {
        return new CompanyMembership(company, client, CompanyMemberRole.OPERATOR, MembershipStatus.ACTIVE);
    }

    private Client saveClient() {
        Client client = buildClient();
        return clientRepository.save(client);
    }

    private Company saveCompany(Client client) {
        Company company = buildCompany(client);
        return companyRepository.save(company);
    }

    private CompanyMembership saveCompanyMembershipViewer(Company company, Client client, CompanyMemberRole role) {
        CompanyMembership membership = buildCompanyMembershipViewer(company, client);
        return companyMembershipRepository.save(membership);
    }

    private CompanyMembership saveCompanyMembershipOperator(Company company, Client client, CompanyMemberRole role) {
        CompanyMembership membership = buildCompanyMembershipOperator(company, client);
        return companyMembershipRepository.save(membership);
    }

    private Client saveDeletedClient() {
        Client client = buildClient();
        client.setDeleted(true);
        return clientRepository.save(client);
    }

    private Company saveDeletedCompany(Client client) {
        Company company = buildCompany(client);
        company.setDeleted(true);
        return companyRepository.save(company);
    }

    private CompanyMembership saveRevokedCompanyMembership(Company company, Client client, CompanyMemberRole role) {
        CompanyMembership membership = buildCompanyMembershipViewer(company, client);
        membership.setStatus(MembershipStatus.REVOKED);
        return companyMembershipRepository.save(membership);
    }


}
