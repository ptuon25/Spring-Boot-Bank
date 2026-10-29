package com.tuon.tbank.service;

import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.enums.person.ClientStatus;
import com.tuon.tbank.exception.serviceException.BusinessException;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.person.ClientRepository;
import com.tuon.tbank.service.person.ClientService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class ClientServiceTests {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // REGISTER TEST

    @Test
    void registerClientCreatesActiveClient() {
        Client client = clientService.registerClient("Active Client",
                LocalDate.of(1990, 1, 1), "10000000001",
                "active.client@tbank.test", "a-strong-password-1");

        assertNotNull(client.getId());
        assertEquals(ClientStatus.ACTIVE, client.getStatus());
        assertEquals(LocalDate.now(), client.getAffiliationDate());
    }

    @Test
    void registerClientEncodesPassword() {
        String rawPassword = "a-strong-password-2";

        Client created = clientService.registerClient("Password Client",
                LocalDate.of(1990, 1, 1), "10000000002",
                "passclient@tbank.test", rawPassword);

        Client saved = clientRepository.findById(created.getId()).orElseThrow();

        assertNotEquals(rawPassword, saved.getPasswordHash());
        assertTrue(passwordEncoder.matches(rawPassword, saved.getPasswordHash()));
    }

    @Test
    void registerClientNormalizesDocument() {
        Client created = clientService.registerClient("Doc Client",
                LocalDate.of(1990, 1, 1), "123.456.789-A",
                "docclient@tbank.test", "a-strong-password-3");

        Client saved = clientRepository.findById(created.getId()).orElseThrow();

        assertEquals("123456789A", saved.getDocument());
    }

    @Test
    void registerClientNormalizesEmail() {
        Client created = clientService.registerClient("Email Client",
                LocalDate.of(1990, 1, 1), "10000000003",
                "eMaIltesT.CLIENT@TBANk.tEST", "a-strong-password-4");

        Client saved = clientRepository.findById(created.getId()).orElseThrow();

        assertEquals("emailtest.client@tbank.test", saved.getEmail());
    }

    @Test
    void registerClientRejectsDuplicateEmail() {
        clientService.registerClient("Test One", LocalDate.of(1990, 1, 1),
                "10000000004", "dup@tbank.test", "a-strong-password-5");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.registerClient("Test Two", LocalDate.of(1990, 1, 1),
                        "10000000005", "dup@tbank.test", "a-strong-password-6"));

        assertEquals("This e-mail is already registered", ex.getMessage());
    }

    @Test
    void registerClientRejectsDuplicateDocument() {
        clientService.registerClient("Test Three", LocalDate.of(1990, 1, 1),
                "123.456.789-B", "test3@tbank.test", "a-strong-password-7");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.registerClient("Test Four", LocalDate.of(1990, 1, 1),
                        "123.456.789-B", "test4@tbank.test", "a-strong-password-8"));

        assertEquals("This document is already registered", ex.getMessage());
    }

    @Test
    void registerClientRejectsDeletedClientEmail() {
        Client deletedClient = clientService.registerClient("Deleted Email Client 1", LocalDate.of(1990, 1, 1),
                "10000000006", "deletedemail@tbank.test", "a-strong-password-9");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        clientService.deleteClient(deletedClient.getId());

        assertThrows(BusinessException.class, () ->
                clientService.registerClient("Deleted Email Client 2", LocalDate.of(1990, 1, 1),
                        "10000000007", "deletedemail@tbank.test", "a-strong-password-10"));
    }

    @Test
    void registerClientRejectsDeletedClientDocument() {
        Client deletedClient = clientService.registerClient("Deleted Document Client 1", LocalDate.of(1990, 1, 1),
                "10000000008", "test5@tbank.test", "a-strong-password-11");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        clientService.deleteClient(deletedClient.getId());

        assertThrows(BusinessException.class, () ->
                clientService.registerClient("Deleted Document Client 2", LocalDate.of(1990, 1, 1),
                        "10000000008", "test6@tbank.test", "a-strong-password-12"));
    }

    @Test
    void registerClientRejectsFutureBirthDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.registerClient("Future Date Client", futureDate,
                        "10000000009", "test7@tbank.test", "a-strong-password-13"));

        assertEquals("Client birthdate cannot be in the future", ex.getMessage());
    }

    @Test
    void registerClientRejectsUnderageClient() {
        LocalDate underAgeDate = LocalDate.now().minusYears(17);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.registerClient("Underage Client", underAgeDate,
                        "10000000010", "test8@tbank.test", "a-strong-password-14"));

        assertEquals("Client must be at least 18 years old", ex.getMessage());
    }

    @Test
    void registerClientAcceptsExactly18YearsOld() {
        LocalDate exactly18YearsOld = LocalDate.now().minusYears(18);

        Client created = assertDoesNotThrow(() ->
                clientService.registerClient("Adult Client", exactly18YearsOld,
                        "10000000011", "test9@tbank.test", "a-strong-password-15"));

        assertEquals(exactly18YearsOld, created.getBirthDate());
    }

    @Test
    void registerClientRejectsInvalidEmailFormat() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.registerClient("Invalid Email Client", LocalDate.of(1990, 1, 1),
                        "10000000012", "test10tbank.test", "a-strong-password-16"));

        assertEquals("Client e-mail has an invalid format", ex.getMessage());
    }

    @Test
    void registerClientRejectsPunctuationOnlyDocument() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.registerClient("Punctual Document Client", LocalDate.of(1990, 1, 1),
                        "...", "test11@tbank.test", "a-strong-password-17"));

        assertEquals("Client document cannot be blank", ex.getMessage());
    }

    @Test
    void registerClientRejectsBlankPassword() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.registerClient("Blank Password Client", LocalDate.of(1990, 1, 1),
                        "10000000012", "test12@tbank.test", "   "));

        assertEquals("Client password cannot be null or blank", ex.getMessage());
    }

    @Test
    void registerClientRejectsPasswordOver72Bytes() {
        String invalidPassword = "ç".repeat(40);

        assertThrows(BusinessException.class, () ->
                clientService.registerClient("Blank Password Client", LocalDate.of(1990, 1, 1),
                        "10000000013", "test13@tbank.test", invalidPassword));

    }

    @Test
    void createClientRejectsNonActiveStatus() {
        Client client = new Client("Blocked Client", LocalDate.of(1990, 1, 1),
                "10000000014", "test14@tbank.test",
                passwordEncoder.encode("a-strong-password-19"),
                LocalDate.now(), ClientStatus.BLOCKED);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> clientService.createClient(client));

        assertEquals("Client cannot be created without ACTIVE status", ex.getMessage());
    }

    @Test
    void createClientRejectsInvalidAffiliationDate() {
        Client nullDate = new Client("Null Date Client", LocalDate.of(1990, 1, 1),
                "10000000015", "test15@tbank.test",
                passwordEncoder.encode("a-strong-password-20"),
                null, ClientStatus.ACTIVE);
        Client futureDate = new Client("Future Date Client", LocalDate.of(1990, 1, 1),
                "10000000016", "test16@tbank.test",
                passwordEncoder.encode("a-strong-password-21"),
                LocalDate.now().plusDays(1), ClientStatus.ACTIVE);

        BusinessException nullEx = assertThrows(BusinessException.class,
                () -> clientService.createClient(nullDate));
        BusinessException futureEx = assertThrows(BusinessException.class,
                () -> clientService.createClient(futureDate));

        assertEquals("Client affiliation date cannot be null", nullEx.getMessage());
        assertEquals("Client affiliation date cannot be in the future", futureEx.getMessage());
    }

    // SEARCH TEST

    @Test
    void getClientByIdReturnsExistingClient() {
        Client created = clientService.registerClient("Id Search Client", LocalDate.of(1990, 1, 1),
                "10000000017", "test17@tbank.test", "a-strong-password-22");

        Client searched = clientService.getClientById(created.getId());

        assertEquals(created.getId(), searched.getId());
        assertEquals("Id Search Client", searched.getName());
        assertEquals("test17@tbank.test", searched.getEmail());
    }

    @Test
    void getClientByIdRejectsInvalidId() {

        assertThrows(BusinessException.class, () ->
                clientService.getClientById(null));
        assertThrows(BusinessException.class, () ->
                clientService.getClientById(0L));
        assertThrows(BusinessException.class, () ->
                clientService.getClientById(-1L));
    }

    @Test
    void getClientByIdRejectsNonexistentClient() {
        ResourceNotFoundException inexistentId = assertThrows(ResourceNotFoundException.class, () ->
                clientService.getClientById(9999L));
    }

    @Test
    void getClientByIdIgnoresDeletedClient() {
        Client deletedClient = clientService.registerClient("Deleted Client", LocalDate.of(1990, 1, 1),
                "10000000018", "test18@tbank.test", "a-strong-password-23");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        clientService.deleteClient(deletedClient.getId());

        assertThrows(ResourceNotFoundException.class, () ->
                clientService.getClientById(deletedClient.getId()));
    }

    @Test
    void getClientByNameRejectsBlankName() {
        assertThrows(BusinessException.class, () ->
                clientService.getClientByName(""));
    }

    @Test
    void getClientByNameRejectsUnknownName() {
        assertThrows(ResourceNotFoundException.class, () ->
                clientService.getClientByName("Unknown Name"));
    }

    @Test
    void getClientByNameExcludesDeletedClients() {
        Client activeClient = clientService.registerClient("Same Name Client", LocalDate.of(1990, 1, 1),
                "10000000020", "test20@tbank.test", "a-strong-password-25");
        Client deletedClient = clientService.registerClient("Same Name Client", LocalDate.of(1990, 1, 1),
                "10000000021", "test21@tbank.test", "a-strong-password-26");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        clientService.deleteClient(deletedClient.getId());

        List<Client> clients = clientService.getClientByName("Same Name Client");

        assertFalse(clients.stream()
                .anyMatch(client -> client.getId().equals(deletedClient.getId())));

        assertTrue(clients.stream()
                .anyMatch(client -> client.getId().equals(activeClient.getId())));
    }

    @Test
    void getClientByStatusFiltersAndSortsByName() {
        Client client1 = clientService.registerClient("B Filter Client 1", LocalDate.of(1990, 1, 1),
                "10000000022", "test22@tbank.test", "a-strong-password-27");
        Client client2 = clientService.registerClient("A Filter Client 2", LocalDate.of(1990, 1, 1),
                "10000000023", "test23@tbank.test", "a-strong-password-28");
        Client client3 = clientService.registerClient("D Filter Client 3", LocalDate.of(1990, 1, 1),
                "10000000024", "test24@tbank.test", "a-strong-password-29");
        Client client4 = clientService.registerClient("C Filter Client 4", LocalDate.of(1990, 1, 1),
                "10000000025", "test25@tbank.test", "a-strong-password-30");
        Client client5 = clientService.registerClient("F Filter Client 5", LocalDate.of(1990, 1, 1),
                "10000000026", "test26@tbank.test", "a-strong-password-31");
        Client client6 = clientService.registerClient("E Filter Client 6", LocalDate.of(1990, 1, 1),
                "10000000027", "test27@tbank.test", "a-strong-password-32");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        clientService.deactivateClient(client3.getId());
        clientService.deactivateClient(client4.getId());
        clientService.blockClient(client5.getId());
        clientService.blockClient(client6.getId());

        List<Client> activeClients = clientService.getClientByStatus(ClientStatus.ACTIVE);
        List<Client> inactiveClients = clientService.getClientByStatus(ClientStatus.INACTIVE);
        List<Client> blockedClients = clientService.getClientByStatus(ClientStatus.BLOCKED);

        assertTrue(activeClients.stream().allMatch(client -> client.getStatus() == ClientStatus.ACTIVE));
        assertTrue(inactiveClients.stream().allMatch(client -> client.getStatus() == ClientStatus.INACTIVE));
        assertTrue(blockedClients.stream().allMatch(client -> client.getStatus() == ClientStatus.BLOCKED));

        assertEquals(2, activeClients.stream()
                .filter(client -> client.getId().equals(client1.getId())
                        || client.getId().equals(client2.getId()))
                .count());
        assertEquals(2, inactiveClients.stream()
                .filter(client -> client.getId().equals(client3.getId())
                        || client.getId().equals(client4.getId()))
                .count());
        assertEquals(2, blockedClients.stream()
                .filter(client -> client.getId().equals(client5.getId())
                        || client.getId().equals(client6.getId()))
                .count());

        List<String> activeNames = activeClients.stream()
                .map(Client::getName)
                .toList();

        List<String> sortedNames = activeNames.stream()
                .sorted()
                .toList();

        assertEquals(sortedNames, activeNames);
    }

    @Test
    void getClientByStatusExcludesDeletedClients() {
        Client activeClient = clientService.registerClient("Same Status Client", LocalDate.of(1990, 1, 1),
                "10000000028", "test28@tbank.test", "a-strong-password-33");
        Client deletedClient = clientService.registerClient("Same Status Client", LocalDate.of(1990, 1, 1),
                "10000000029", "test29@tbank.test", "a-strong-password-34");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        clientService.deleteClient(deletedClient.getId());

        List<Client> clients = clientService.getClientByStatus(ClientStatus.ACTIVE);

        assertFalse(clients.stream()
                .anyMatch(client -> client.getId().equals(deletedClient.getId())));

        assertTrue(clients.stream()
                .anyMatch(client -> client.getId().equals(activeClient.getId())));
    }

    @Test
    void getClientByEmailIgnoresCaseAndSpaces() {
        Client client = clientService.registerClient("Ignore Case and Space Client", LocalDate.of(1990, 1, 1),
                "10000000030", "test30@tbank.test", "a-strong-password-35");

        Client searched = clientService.getClientByEmail(" test30@TBANk.TESt  ");

        assertEquals(client.getEmail(), searched.getEmail());
    }

    @Test
    void getClientByDocumentAcceptsFormattedDocument() {
        Client client = clientService.registerClient("Formatted Documente Client", LocalDate.of(1990, 1, 1),
                "100.000.000-31", "test31@tbank.test", "a-strong-password-36");

        Client searched = clientService.getClientByDocument("10000000031");

        assertEquals(client.getDocument(), searched.getDocument());
    }

    @Test
    void getAllClientsIncludesDeletedClients() {
        Client client1 = clientService.registerClient("B Filter Client 1", LocalDate.of(1990, 1, 1),
                "10000000032", "test32@tbank.test", "a-strong-password-37");
        Client client2 = clientService.registerClient("A Filter Client 2", LocalDate.of(1990, 1, 1),
                "10000000033", "test33@tbank.test", "a-strong-password-38");
        Client client3 = clientService.registerClient("D Filter Client 3", LocalDate.of(1990, 1, 1),
                "10000000034", "test34@tbank.test", "a-strong-password-39");
        Client client4 = clientService.registerClient("C Filter Client 4", LocalDate.of(1990, 1, 1),
                "10000000035", "test35@tbank.test", "a-strong-password-40");
        Client client5 = clientService.registerClient("F Filter Client 5", LocalDate.of(1990, 1, 1),
                "10000000036", "test36@tbank.test", "a-strong-password-41");
        Client client6 = clientService.registerClient("E Filter Client 6", LocalDate.of(1990, 1, 1),
                "10000000037", "test37@tbank.test", "a-strong-password-42");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        clientService.deleteClient(client4.getId());
        clientService.deleteClient(client5.getId());
        clientService.deleteClient(client6.getId());

        List<Client> clients = clientService.getAllClients();

        assertEquals(3, clients.stream()
                .filter(client -> client.getId().equals(client1.getId())
                        || client.getId().equals(client2.getId())
                        || client.getId().equals(client3.getId()))
                .count());
        assertEquals(3, clients.stream()
                .filter(client -> client.getId().equals(client4.getId())
                        || client.getId().equals(client5.getId())
                        || client.getId().equals(client6.getId()))
                .count());
        assertTrue(clients.stream()
                .anyMatch(client -> client.getId().equals(client4.getId())
                        && client.isDeleted()));
    }

    @Test
    void getDeletedClientsReturnsOnlyDeletedClients() {
        Client client1 = clientService.registerClient("B Filter Client 1", LocalDate.of(1990, 1, 1),
                "10000000038", "test38@tbank.test", "a-strong-password-43");
        Client client2 = clientService.registerClient("A Filter Client 2", LocalDate.of(1990, 1, 1),
                "10000000039", "test39@tbank.test", "a-strong-password-44");
        Client client3 = clientService.registerClient("D Filter Client 3", LocalDate.of(1990, 1, 1),
                "10000000040", "test40@tbank.test", "a-strong-password-45");
        Client client4 = clientService.registerClient("C Filter Client 4", LocalDate.of(1990, 1, 1),
                "10000000041", "test41@tbank.test", "a-strong-password-46");
        Client client5 = clientService.registerClient("F Filter Client 5", LocalDate.of(1990, 1, 1),
                "10000000042", "test42@tbank.test", "a-strong-password-47");
        Client client6 = clientService.registerClient("E Filter Client 6", LocalDate.of(1990, 1, 1),
                "10000000043", "test43@tbank.test", "a-strong-password-48");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        clientService.deleteClient(client4.getId());
        clientService.deleteClient(client5.getId());
        clientService.deleteClient(client6.getId());

        List<Client> deletedClients = clientService.getDeletedClients();

        assertEquals(3, deletedClients.stream()
                .filter(client -> client.getId().equals(client4.getId())
                        || client.getId().equals(client5.getId())
                        || client.getId().equals(client6.getId()))
                .count());
    }

    // UPDATE TEST

    @Test
    void updateClientChangesEditableFields() {
        Client created = clientService.registerClient("Original Client", LocalDate.of(1990, 1, 1),
                "10000000044A", "test44a@tbank.test", "a-strong-password-49");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        Client newData = new Client();
        newData.setName("Updated Client");
        newData.setEmail("test44b@tbank.test");
        newData.setDocument("10000000044B");
        newData.setBirthDate(LocalDate.of(1990, 2, 1));

        clientService.updateClient(created.getId(), newData);

        Client saved = clientRepository.findById(created.getId()).orElseThrow();

        assertEquals("Updated Client", saved.getName());
        assertEquals("test44b@tbank.test", saved.getEmail());
        assertEquals("10000000044B", saved.getDocument());
        assertEquals(LocalDate.of(1990, 2, 1), saved.getBirthDate());
    }

    @Test
    void updateClientKeepsStatusAffiliationDateAndPassword() {
        Client created = clientService.registerClient(
                "Original Client",
                LocalDate.of(1990, 1, 1),
                "10000000045A",
                "test45a@tbank.test",
                "a-strong-password-50");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        ClientStatus originalStatus = created.getStatus();
        LocalDate originalAffiliationDate = created.getAffiliationDate();
        String originalPassword = created.getPasswordHash();

        Client newData = new Client();
        newData.setName("Updated Client");
        newData.setEmail("test45b@tbank.test");
        newData.setDocument("10000000045B");
        newData.setBirthDate(LocalDate.of(1990, 2, 1));
        newData.setStatus(ClientStatus.BLOCKED);
        newData.setAffiliationDate(LocalDate.of(2000, 1, 1));
        newData.changePasswordHash("hash-que-nao-deve-ser-aplicado");

        clientService.updateClient(created.getId(), newData);

        Client saved = clientRepository.findById(created.getId()).orElseThrow();

        assertEquals(originalStatus, saved.getStatus());
        assertEquals(originalAffiliationDate, saved.getAffiliationDate());
        assertEquals(originalPassword, saved.getPasswordHash());
    }

    @Test
    void updateClientAllowsKeepingOwnEmailAndDocument() {
        Client created = clientService.registerClient("Original Client",
                LocalDate.of(1990, 1, 1), "10000000046",
                "test46@tbank.test", "a-strong-password-51");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        Client sameData = new Client();
        sameData.setName("Updated Client");
        sameData.setDocument(created.getDocument());
        sameData.setEmail(created.getEmail());
        sameData.setBirthDate(created.getBirthDate());

        assertDoesNotThrow(() -> clientService.updateClient(created.getId(), sameData));
    }

    @Test
    void updateClientRejectsOtherClientsEmail() {
        Client created1 = clientService.registerClient("Client 1",
                LocalDate.of(1990, 1, 1), "10000000047",
                "test46a@tbank.test", "a-strong-password-52");
        Client created2 = clientService.registerClient("Client 2",
                LocalDate.of(1990, 1, 1), "10000000048",
                "test46b@tbank.test", "a-strong-password-53");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        Client newData = new Client();
        newData.setName("Client 1");
        newData.setBirthDate(LocalDate.of(1990, 1, 1));
        newData.setDocument("10000000047");
        newData.setEmail(created2.getEmail());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.updateClient(created1.getId(), newData));

        assertEquals("This e-mail is already registered by another client", ex.getMessage());
    }

    @Test
    void updateClientRejectsOtherClientsDocument() {
        Client created1 = clientService.registerClient("Client 1",
                LocalDate.of(1990, 1, 1), "10000000049",
                "test47a@tbank.test", "a-strong-password-54");
        Client created2 = clientService.registerClient("Client 2",
                LocalDate.of(1990, 1, 1), "10000000050",
                "test47b@tbank.test", "a-strong-password-55");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        Client newData = new Client();
        newData.setName("Client 1");
        newData.setBirthDate(LocalDate.of(1990, 1, 1));
        newData.setDocument(created2.getDocument());
        newData.setEmail(created1.getEmail());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.updateClient(created1.getId(), newData));

        assertEquals("This document is already registered by another client", ex.getMessage());
    }

    @Test
    void updateClientRejectsUnderageBirthDate() {
        Client created = clientService.registerClient("Client Underage Update",
                LocalDate.of(1990, 1, 1), "10000000051",
                "test48@tbank.test", "a-strong-password-56");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        Client newData = new Client();
        newData.setName("Client Underage Update");
        newData.setBirthDate(LocalDate.now().minusYears(17));
        newData.setDocument(created.getDocument());
        newData.setEmail(created.getEmail());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.updateClient(created.getId(), newData));

        assertEquals("Client must be at least 18 years old", ex.getMessage());
    }

    @Test
    void updateClientRejectsNullClient() {
        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.updateClient(1L, null));

        assertEquals("Client cannot be null", ex.getMessage());
    }

    @Test
    void updateClientRejectsDeletedClient() {
        Client created = clientService.registerClient("Client Deleted Update",
                LocalDate.of(1990, 1, 1), "10000000052",
                "test49@tbank.test", "a-strong-password-57");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");
        clientService.deleteClient(created.getId());

        Client newData = new Client();
        newData.setName("Client Deleted Update");
        newData.setBirthDate(LocalDate.of(1990, 1, 1));
        newData.setDocument(created.getDocument());
        newData.setEmail(created.getEmail());

        assertThrows(ResourceNotFoundException.class, () ->
                clientService.updateClient(created.getId(), newData));
    }

    // STATUS TEST

    @Test
    void blockClientSetsBlockedStatus() {
        Client created = clientService.registerClient(
                "Block Status Client",
                LocalDate.now().minusYears(30),
                "10000000053",
                "test50@tbank.test",
                "a-strong-password-58");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        clientService.blockClient(created.getId());

        Client saved = clientRepository.findById(created.getId()).orElseThrow();

        assertEquals(ClientStatus.BLOCKED, saved.getStatus());
    }

    @Test
    void blockClientRejectsAlreadyBlockedClient() {
        Client created = clientService.registerClient(
                "Already Blocked Status Client",
                LocalDate.now().minusYears(30),
                "10000000054",
                "test51@tbank.test",
                "a-strong-password-59");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");
        clientService.blockClient(created.getId());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.blockClient(created.getId()));

        assertEquals("Client is already blocked", ex.getMessage());
    }

    @Test
    void deactivateClientSetsInactiveStatus() {
        Client created = clientService.registerClient(
                "Deactivate Status Client",
                LocalDate.now().minusYears(30),
                "10000000055",
                "test52@tbank.test",
                "a-strong-password-60");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        clientService.deactivateClient(created.getId());

        Client saved = clientRepository.findById(created.getId()).orElseThrow();

        assertEquals(ClientStatus.INACTIVE, saved.getStatus());
    }

    @Test
    void deactivateClientRejectsAlreadyInactiveClient() {
        Client created = clientService.registerClient(
                "Already Inactive Status Client",
                LocalDate.now().minusYears(30),
                "10000000056",
                "test53@tbank.test",
                "a-strong-password-61");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");
        clientService.deactivateClient(created.getId());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.deactivateClient(created.getId()));

        assertEquals("Client is already inactive", ex.getMessage());
    }

    @Test
    void deactivateClientRejectsBlockedClient() {
        Client created = clientService.registerClient(
                "Blocked Cannot Deactivate Client",
                LocalDate.now().minusYears(30),
                "10000000057",
                "test54@tbank.test",
                "a-strong-password-62");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");
        clientService.blockClient(created.getId());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.deactivateClient(created.getId()));

        assertEquals("Cannot deactivate a blocked client", ex.getMessage());
    }

    @Test
    void blockClientAcceptsInactiveClient() {
        Client created = clientService.registerClient(
                "Inactive Can Be Blocked Client",
                LocalDate.now().minusYears(30),
                "10000000058",
                "test55@tbank.test",
                "a-strong-password-63");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");
        clientService.deactivateClient(created.getId());
        clientService.blockClient(created.getId());

        Client saved = clientRepository.findById(created.getId()).orElseThrow();

        assertEquals(ClientStatus.BLOCKED, saved.getStatus());
    }

    @Test
    void reactivateClientFromInactive() {
        Client created = clientService.registerClient(
                "Reactivate Inactive Client",
                LocalDate.now().minusYears(30),
                "10000000059",
                "test56@tbank.test",
                "a-strong-password-64");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");
        clientService.deactivateClient(created.getId());
        clientService.reactivateClient(created.getId());

        Client saved = clientRepository.findById(created.getId()).orElseThrow();

        assertEquals(ClientStatus.ACTIVE, saved.getStatus());
    }

    @Test
    void reactivateClientFromBlocked() {
        Client created = clientService.registerClient(
                "Reactivate Blocked Client",
                LocalDate.now().minusYears(30),
                "10000000060",
                "test57@tbank.test",
                "a-strong-password-65");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");
        clientService.blockClient(created.getId());
        clientService.reactivateClient(created.getId());

        Client saved = clientRepository.findById(created.getId()).orElseThrow();

        assertEquals(ClientStatus.ACTIVE, saved.getStatus());
    }

    @Test
    void reactivateClientRejectsAlreadyActiveClient() {
        Client created = clientService.registerClient(
                "Already Active Status Client",
                LocalDate.now().minusYears(30),
                "10000000061",
                "test58@tbank.test",
                "a-strong-password-66");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.reactivateClient(created.getId()));

        assertEquals("Client is already active", ex.getMessage());
    }

    // DELETE TEST

    @Test
    void deleteClientMarksClientAsDeleted() {
        Client created = clientService.registerClient(
                "Delete Marks Client",
                LocalDate.now().minusYears(30),
                "10000000062",
                "test59@tbank.test",
                "a-strong-password-67");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        clientService.deleteClient(created.getId());

        Client saved = clientRepository.findById(created.getId()).orElseThrow();

        assertTrue(saved.isDeleted());
    }

    @Test
    void deleteClientKeepsTheRecord() {
        Client created = clientService.registerClient(
                "Delete Keeps Record Client",
                LocalDate.now().minusYears(30),
                "10000000063",
                "test60@tbank.test",
                "a-strong-password-68");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        clientService.deleteClient(created.getId());

        Client saved = clientRepository.findById(created.getId()).orElseThrow();

        assertEquals(created.getId(), saved.getId());
        assertTrue(saved.isDeleted());
    }

    @Test
    void deleteClientRejectsAlreadyDeletedClient() {
        Client created = clientService.registerClient(
                "Already Deleted Client",
                LocalDate.now().minusYears(30),
                "10000000064",
                "test61@tbank.test",
                "a-strong-password-69");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");
        clientService.deleteClient(created.getId());

        assertThrows(ResourceNotFoundException.class, () ->
                clientService.deleteClient(created.getId()));
    }

    @Test
    void deleteClientRejectsNonexistentClient() {
        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        assertThrows(ResourceNotFoundException.class, () ->
                clientService.deleteClient(Long.MAX_VALUE));
    }

    // PASSWORD TEST

    @Test
    void changeOwnPasswordUpdatesHash() {
        String oldPassword = "a-strong-password-70";
        String newPassword = "a-strong-password-71";
        Client created = clientService.registerClient(
                "Change Password Client",
                LocalDate.now().minusYears(30),
                "10000000065",
                "test62@tbank.test",
                oldPassword);

        authenticateAs("CLIENT", created.getId(), "ROLE_CLIENT");

        clientService.changeOwnPassword(created.getId(), oldPassword, newPassword);

        Client saved = clientRepository.findById(created.getId()).orElseThrow();

        assertTrue(passwordEncoder.matches(newPassword, saved.getPasswordHash()));
        assertFalse(passwordEncoder.matches(oldPassword, saved.getPasswordHash()));
    }

    @Test
    void changeOwnPasswordRejectsWrongCurrentPassword() {
        Client created = clientService.registerClient(
                "Wrong Current Password Client",
                LocalDate.now().minusYears(30),
                "10000000066",
                "test63@tbank.test",
                "a-strong-password-72");

        authenticateAs("CLIENT", created.getId(), "ROLE_CLIENT");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.changeOwnPassword(
                        created.getId(), "wrong-current-password", "a-strong-password-73"));

        assertEquals("Current password is incorrect", ex.getMessage());
    }

    @Test
    void changeOwnPasswordRejectsNullCurrentPassword() {
        Client created = clientService.registerClient(
                "Null Current Password Client",
                LocalDate.now().minusYears(30),
                "10000000067",
                "test64@tbank.test",
                "a-strong-password-74");

        authenticateAs("CLIENT", created.getId(), "ROLE_CLIENT");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.changeOwnPassword(created.getId(), null, "a-strong-password-75"));

        assertEquals("Current password is incorrect", ex.getMessage());
    }

    @Test
    void changeOwnPasswordRejectsSamePassword() {
        String currentPassword = "a-strong-password-76";
        Client created = clientService.registerClient(
                "Same Password Client",
                LocalDate.now().minusYears(30),
                "10000000068",
                "test65@tbank.test",
                currentPassword);

        authenticateAs("CLIENT", created.getId(), "ROLE_CLIENT");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.changeOwnPassword(created.getId(), currentPassword, currentPassword));

        assertEquals("New password must be different from the current password", ex.getMessage());
    }

    @Test
    void changeOwnPasswordRejectsBlankNewPassword() {
        String currentPassword = "a-strong-password-77";
        Client created = clientService.registerClient(
                "Blank New Password Client",
                LocalDate.now().minusYears(30),
                "10000000069",
                "test66@tbank.test",
                currentPassword);

        authenticateAs("CLIENT", created.getId(), "ROLE_CLIENT");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.changeOwnPassword(created.getId(), currentPassword, "   "));

        assertEquals("Client password cannot be null or blank", ex.getMessage());
    }

    @Test
    void changeOwnPasswordRejectsNewPasswordOver72Bytes() {
        String currentPassword = "a-strong-password-78";
        Client created = clientService.registerClient(
                "Long New Password Client",
                LocalDate.now().minusYears(30),
                "10000000070",
                "test67@tbank.test",
                currentPassword);

        authenticateAs("CLIENT", created.getId(), "ROLE_CLIENT");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.changeOwnPassword(created.getId(), currentPassword, "ç".repeat(40)));

        assertEquals("Client password exceeds BCrypt's supported input length", ex.getMessage());
    }

    @Test
    void changeOwnPasswordKeepsHashWhenRejected() {
        Client created = clientService.registerClient(
                "Rejected Password Change Client",
                LocalDate.now().minusYears(30),
                "10000000071",
                "test68@tbank.test",
                "a-strong-password-79");
        String originalHash = created.getPasswordHash();

        authenticateAs("CLIENT", created.getId(), "ROLE_CLIENT");

        assertThrows(BusinessException.class, () ->
                clientService.changeOwnPassword(created.getId(), "incorrect-password", "a-strong-password-80"));

        Client saved = clientRepository.findById(created.getId()).orElseThrow();

        assertEquals(originalHash, saved.getPasswordHash());
    }

    @Test
    void resetClientPasswordUpdatesHash() {
        Client created = clientService.registerClient(
                "Reset Password Client",
                LocalDate.now().minusYears(30),
                "10000000072",
                "test69@tbank.test",
                "a-strong-password-81");
        String newPassword = "a-strong-password-82";

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        clientService.resetClientPassword(created.getId(), newPassword);

        Client saved = clientRepository.findById(created.getId()).orElseThrow();

        assertTrue(passwordEncoder.matches(newPassword, saved.getPasswordHash()));
        assertFalse(passwordEncoder.matches("a-strong-password-81", saved.getPasswordHash()));
    }

    @Test
    void resetClientPasswordRejectsBlankPassword() {
        Client created = clientService.registerClient(
                "Reset Blank Password Client",
                LocalDate.now().minusYears(30),
                "10000000073",
                "test70@tbank.test",
                "a-strong-password-83");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.resetClientPassword(created.getId(), " "));

        assertEquals("Client password cannot be null or blank", ex.getMessage());
    }

    @Test
    void resetClientPasswordRejectsPasswordOver72Bytes() {
        Client created = clientService.registerClient(
                "Reset Long Password Client",
                LocalDate.now().minusYears(30),
                "10000000074",
                "test71@tbank.test",
                "a-strong-password-84");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                clientService.resetClientPassword(created.getId(), "ç".repeat(40)));

        assertEquals("Client password exceeds BCrypt's supported input length", ex.getMessage());
    }

    @Test
    void resetClientPasswordRejectsDeletedClient() {
        Client created = clientService.registerClient(
                "Reset Deleted Password Client",
                LocalDate.now().minusYears(30),
                "10000000075",
                "test72@tbank.test",
                "a-strong-password-85");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");
        clientService.deleteClient(created.getId());

        assertThrows(ResourceNotFoundException.class, () ->
                clientService.resetClientPassword(created.getId(), "a-strong-password-86"));
    }

    // AUTHORIZATION TEST

    @Test
    void updateClientDeniesClientIdentity() {
        Client created = clientService.registerClient(
                "Update Denied Client Identity",
                LocalDate.now().minusYears(30),
                "10000000076",
                "test73@tbank.test",
                "a-strong-password-87");
        Client newData = new Client();
        newData.setName("Updated Denied Client Identity");
        newData.setBirthDate(created.getBirthDate());
        newData.setDocument(created.getDocument());
        newData.setEmail(created.getEmail());

        authenticateAs("CLIENT", created.getId(), "ROLE_CLIENT");

        assertThrows(AccessDeniedException.class, () ->
                clientService.updateClient(created.getId(), newData));
    }

    @Test
    void updateClientDeniesAssociate() {
        Client created = clientService.registerClient(
                "Update Denied Associate",
                LocalDate.now().minusYears(30),
                "10000000077",
                "test74@tbank.test",
                "a-strong-password-88");
        Client newData = new Client();
        newData.setName("Updated Denied Associate");
        newData.setBirthDate(created.getBirthDate());
        newData.setDocument(created.getDocument());
        newData.setEmail(created.getEmail());

        authenticateAs("EMPLOYEE", 1, "ROLE_ASSOCIATE");

        assertThrows(AccessDeniedException.class, () ->
                clientService.updateClient(created.getId(), newData));
    }

    @Test
    void deleteClientDeniesClientIdentity() {
        Client created = clientService.registerClient(
                "Delete Denied Client Identity",
                LocalDate.now().minusYears(30),
                "10000000078",
                "test75@tbank.test",
                "a-strong-password-89");

        authenticateAs("CLIENT", created.getId(), "ROLE_CLIENT");

        assertThrows(AccessDeniedException.class, () ->
                clientService.deleteClient(created.getId()));
    }

    @Test
    void deleteClientDeniesAssociate() {
        Client created = clientService.registerClient(
                "Delete Denied Associate",
                LocalDate.now().minusYears(30),
                "10000000079",
                "test76@tbank.test",
                "a-strong-password-90");

        authenticateAs("EMPLOYEE", 1, "ROLE_ASSOCIATE");

        assertThrows(AccessDeniedException.class, () ->
                clientService.deleteClient(created.getId()));
    }

    @Test
    void resetClientPasswordDeniesClientIdentity() {
        Client created = clientService.registerClient(
                "Reset Denied Client Identity",
                LocalDate.now().minusYears(30),
                "10000000080",
                "test77@tbank.test",
                "a-strong-password-91");

        authenticateAs("CLIENT", created.getId(), "ROLE_CLIENT");

        assertThrows(AccessDeniedException.class, () ->
                clientService.resetClientPassword(created.getId(), "a-strong-password-92"));
    }

    @Test
    void resetClientPasswordDeniesAssociate() {
        Client created = clientService.registerClient(
                "Reset Denied Associate",
                LocalDate.now().minusYears(30),
                "10000000081",
                "test78@tbank.test",
                "a-strong-password-93");

        authenticateAs("EMPLOYEE", 1, "ROLE_ASSOCIATE");

        assertThrows(AccessDeniedException.class, () ->
                clientService.resetClientPassword(created.getId(), "a-strong-password-94"));
    }

    @Test
    void changeOwnPasswordDeniesOtherClient() {
        Client created = clientService.registerClient(
                "Other Client Password Target",
                LocalDate.now().minusYears(30),
                "10000000082",
                "test79@tbank.test",
                "a-strong-password-95");

        authenticateAs("CLIENT", created.getId() + 1, "ROLE_CLIENT");

        assertThrows(AccessDeniedException.class, () ->
                clientService.changeOwnPassword(
                        created.getId(), "a-strong-password-95", "a-strong-password-96"));
    }

    @Test
    void changeOwnPasswordDeniesEmployee() {
        Client created = clientService.registerClient(
                "Employee Password Target",
                LocalDate.now().minusYears(30),
                "10000000083",
                "test80@tbank.test",
                "a-strong-password-97");

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        assertThrows(AccessDeniedException.class, () ->
                clientService.changeOwnPassword(
                        created.getId(), "a-strong-password-97", "a-strong-password-98"));
    }

    @Test
    void managerCanUpdateBlockAndDeleteClients() {
        Client created = clientService.registerClient(
                "Manager Operations Client",
                LocalDate.now().minusYears(30),
                "10000000084",
                "test81@tbank.test",
                "a-strong-password-99");
        Client newData = new Client();
        newData.setName("Manager Updated Operations Client");
        newData.setBirthDate(created.getBirthDate());
        newData.setDocument(created.getDocument());
        newData.setEmail(created.getEmail());

        authenticateAs("EMPLOYEE", 1, "ROLE_MANAGER");

        assertDoesNotThrow(() -> clientService.updateClient(created.getId(), newData));
        assertDoesNotThrow(() -> clientService.blockClient(created.getId()));
        assertDoesNotThrow(() -> clientService.deleteClient(created.getId()));

        Client saved = clientRepository.findById(created.getId()).orElseThrow();

        assertEquals("Manager Updated Operations Client", saved.getName());
        assertEquals(ClientStatus.BLOCKED, saved.getStatus());
        assertTrue(saved.isDeleted());
    }

    // AUXILIARY

    private void authenticateAs(String identityType, long userId, String authority) {
        Jwt token = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("identity_type", identityType)
                .claim("user_id", userId)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(token, List.of(new SimpleGrantedAuthority(authority))));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}
