package com.tuon.tbank.repository;

import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.enums.person.ClientStatus;
import com.tuon.tbank.repository.person.ClientRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class ClientRepositoryTests {

    private static final AtomicLong COUNTER = new AtomicLong(5000);

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findByIdAndDeletedFalseReturnsActiveClient() {
        Client saved = saveClient();

        Optional<Client> found = clientRepository.findByIdAndDeletedFalse(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getEmail(), found.get().getEmail());
    }

    @Test
    void findByIdAndDeletedFalseIgnoresDeletedClient() {
        Client deleted = saveDeletedClient();

        Optional<Client> found = clientRepository.findByIdAndDeletedFalse(deleted.getId());

        assertTrue(found.isEmpty());
    }

    @Test
    void findByEmailAndDeletedFalseIgnoresDeletedClient() {
        Client deleted = saveDeletedClient();

        Optional<Client> found = clientRepository.findByEmailAndDeletedFalse(deleted.getEmail());

        assertTrue(found.isEmpty());
    }

    @Test
    void findByDocumentAndDeletedFalseIgnoresDeletedClient() {
        Client deleted = saveDeletedClient();

        Optional<Client> found = clientRepository.findByDocumentAndDeletedFalse(deleted.getDocument());

        assertTrue(found.isEmpty());
    }

    @Test
    void findByEmailFindsDeletedClient() {
        Client deleted = saveDeletedClient();

        Optional<Client> found = clientRepository.findByEmail(deleted.getEmail());

        assertTrue(found.isPresent());
        assertTrue(found.get().isDeleted());
    }

    @Test
    void findByNameAndDeletedFalseReturnsAllWithSameName() {
        Client saved1 = saveClient();
        Client saved2 = saveClient();
        saved2.setName(saved1.getName());
        clientRepository.saveAndFlush(saved2);

        var found = clientRepository.findByNameAndDeletedFalse(saved1.getName());

        assertEquals(2, found.size());
    }

    @Test
    void findByStatusOrderedByNameAndNotDeleted() {
        Client zzz = buildClient();
        zzz.setName("ZZZ Sort Client");
        clientRepository.saveAndFlush(zzz);

        Client aaa = buildClient();
        aaa.setName("AAA Sort Client");
        clientRepository.saveAndFlush(aaa);

        Client deleted = buildClient();
        deleted.setName("MMM Sort Client");
        deleted.setDeleted(true);
        clientRepository.saveAndFlush(deleted);

        List<Client> found = clientRepository.findByStatusAndDeletedFalseOrderByNameAsc(ClientStatus.ACTIVE);

        assertTrue(found.contains(aaa));
        assertTrue(found.contains(zzz));
        assertFalse(found.contains(deleted));
        assertTrue(found.indexOf(aaa) < found.indexOf(zzz));
    }

    @Test
    void findByDeletedTrueReturnsOnlyDeletedClients() {
        Client deleted1 = saveDeletedClient();
        Client deleted2 = saveDeletedClient();
        Client active = saveClient();

        List<Client> found = clientRepository.findByDeletedTrue();

        assertTrue(found.contains(deleted1));
        assertTrue(found.contains(deleted2));
        assertFalse(found.contains(active));
    }

    @Test
    void existsByEmailIsTrueForDeletedClient() {
        Client deleted = saveDeletedClient();

        assertTrue(clientRepository.existsByEmail(deleted.getEmail()));
    }

    @Test
    void existsByDocumentIsTrueForDeletedClient() {
        Client deleted = saveDeletedClient();

        assertTrue(clientRepository.existsByDocument(deleted.getDocument()));
    }

    @Test
    void existsByEmailAndIdNotIgnoresOwnId() {
        Client owner = saveClient();
        Client other = saveClient();

        assertFalse(clientRepository.existsByEmailAndIdNot(owner.getEmail(), owner.getId()));
        assertTrue(clientRepository.existsByEmailAndIdNot(owner.getEmail(), other.getId()));
    }

    @Test
    void existsByDocumentAndIdNotIgnoresOwnId() {
        Client owner = saveClient();
        Client other = saveClient();

        assertFalse(clientRepository.existsByDocumentAndIdNot(owner.getDocument(), owner.getId()));
        assertTrue(clientRepository.existsByDocumentAndIdNot(owner.getDocument(), other.getId()));
    }

    @Test
    void saveRejectsDuplicateEmail() {
        Client client1 = saveClient();
        Client client2 = buildClient();
        client2.setEmail(client1.getEmail());

        assertThrows(DataIntegrityViolationException.class, () -> clientRepository.saveAndFlush(client2));
    }

    @Test
    void saveRejectsNullEmail() {
        Client client = buildClient();
        client.setEmail(null);

        assertThrows(DataIntegrityViolationException.class, () -> clientRepository.saveAndFlush(client));
    }

    @Test
    void saveAllowsDuplicateDocumentAtDatabaseLevel() {
        Client client1 = saveClient();
        Client client2 = buildClient();
        client2.setDocument(client1.getDocument());

        assertDoesNotThrow(() -> clientRepository.saveAndFlush(client2));
    }

    @Test
    void newClientIsNotDeletedByDefault() {
        Client saved = saveClient();

        Boolean deletedInDb = jdbcTemplate.queryForObject(
                "SELECT deleted FROM person WHERE id = ?", Boolean.class, saved.getId());

        assertFalse(deletedInDb);
    }

    @Test
    void statusIsStoredAsString() {
        Client saved = saveClient();

        String statusInDb = jdbcTemplate.queryForObject(
                "SELECT status FROM clients WHERE id = ?", String.class, saved.getId());

        assertEquals("ACTIVE", statusInDb);
    }

    @Test
    void saveClientWritesToPersonAndClientsTables() {
        Client saved = saveClient();

        Integer personRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM person WHERE id = ?", Integer.class, saved.getId());
        Integer clientRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM clients WHERE id = ?", Integer.class, saved.getId());

        String emailInPerson = jdbcTemplate.queryForObject(
                "SELECT email FROM person WHERE id = ?", String.class, saved.getId());
        String statusInClients = jdbcTemplate.queryForObject(
                "SELECT status FROM clients WHERE id = ?", String.class, saved.getId());

        assertEquals(1, personRows);
        assertEquals(1, clientRows);
        assertEquals(saved.getEmail(), emailInPerson);
        assertEquals("ACTIVE", statusInClients);
    }

    @Test
    void findByIdReturnsAllInheritedFields() {
        Client saved = saveClient();
        entityManager.flush();
        entityManager.clear();

        Client found = clientRepository.findById(saved.getId()).orElseThrow();

        assertEquals(saved.getName(), found.getName());
        assertEquals(saved.getBirthDate(), found.getBirthDate());
        assertEquals(saved.getDocument(), found.getDocument());
        assertEquals(saved.getEmail(), found.getEmail());
        assertEquals(saved.getPasswordHash(), found.getPasswordHash());
        assertEquals(saved.getStatus(), found.getStatus());
    }

    // AUXILIARY

    private Client buildClient() {
        long n = COUNTER.incrementAndGet();
        return new Client("Repo Client " + n, LocalDate.of(1990, 1, 1),
                "3000" + n, "repo" + n + "@tbank.test", "fake-hash",
                LocalDate.now(), ClientStatus.ACTIVE);
    }

    private Client saveClient() {
        return clientRepository.saveAndFlush(buildClient());
    }

    private Client saveDeletedClient() {
        Client client = buildClient();
        client.setDeleted(true);
        return clientRepository.saveAndFlush(client);
    }
}
