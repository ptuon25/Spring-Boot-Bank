package com.tuon.tbank.service.person;

import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.enums.person.ClientStatus;
import com.tuon.tbank.exception.serviceException.BusinessException;
import com.tuon.tbank.exception.serviceException.DatabaseException;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.person.ClientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientService(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client getClientById(Long id) {
        Optional<Client> client = clientRepository.findById(id);
        return client.orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }

    public Client createClient(Client client) {

        validateForCreateClient(client);
        try {
            return clientRepository.saveAndFlush(client);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not create client due to a database constraint.");
        }
    }

    public Client registerClient(String name, LocalDate birthDate, String document, String email, String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("Client password exceeds BCrypt's supported input length");
        }
        Client client = new Client(name, birthDate, document, email.trim().toLowerCase(Locale.ROOT),
                passwordEncoder.encode(password), LocalDate.now(), ClientStatus.ACTIVE);
        return createClient(client);
    }

    public Client updateClient(Long id, Client client) {
        if (client == null) {
            throw new BusinessException("Client cannot be null");
        }
        Client entity = getClientById(id);

        validateForUpdateClient(client, id);
        updateData(entity, client);
        try {
            return clientRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not update client due to a database constraint.");
        }
    }

    public void deleteClient(Long id) {
        Client client = getClientById(id);
        try {
            clientRepository.delete(client);
            clientRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not delete client due to a database constraint.");
        }
    }

    private void validateForCreateClient(Client client) {
        if (client == null) {
            throw new BusinessException("Client cannot be null");
        }
        if (client.getName() == null || client.getName().isBlank()) {
            throw new BusinessException("Client name cannot be null or blank");
        }
        if (client.getBirthDate() == null) {
            throw new BusinessException("Client birthdate cannot be null");
        }
        if (client.getBirthDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Client birthdate cannot be in the future");
        }
        if (client.getDocument() == null || client.getDocument().isBlank()) {
            throw new BusinessException("Client document cannot be null or blank");
        }
        validateEmail(client.getEmail());
        validateEmailNotRegistered(client.getEmail());
        if (client.getPasswordHash() == null || client.getPasswordHash().isBlank()) {
            throw new BusinessException("Client password hash cannot be null or blank");
        }
        if (client.getAffiliationDate() == null) {
            throw new BusinessException("Client affiliation date cannot be null");
        }
        if (client.getAffiliationDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Client affiliation date cannot be in the future");
        }
        if (client.getStatus() != ClientStatus.ACTIVE) {
            throw new BusinessException("Client cannot be created without ACTIVE status");
        }
    }

    public void validateForUpdateClient(Client client, Long id) {
        if (client.getName() == null || client.getName().isBlank()) {
            throw new BusinessException("Client name cannot be null or blank");
        }
        if (client.getBirthDate() == null) {
            throw new BusinessException("Client birthdate cannot be null");
        }
        if (client.getBirthDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Client birthdate cannot be in the future");
        }
        if (client.getDocument() == null || client.getDocument().isBlank()) {
            throw new BusinessException("Client document cannot be null or blank");
        }
        validateEmail(client.getEmail());
        validateEmailNotRegisteredForUpdate(client.getEmail(), id);
        if (client.getAffiliationDate() == null) {
            throw new BusinessException("Client affiliation date cannot be null");
        }
        if (client.getAffiliationDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Client affiliation date cannot be in the future");
        }
    }

    private void updateData(Client entity, Client client) {
        entity.setName(client.getName());
        entity.setBirthDate(client.getBirthDate());
        entity.setDocument(client.getDocument());
        entity.setEmail(client.getEmail());
        entity.setAffiliationDate(client.getAffiliationDate());
        entity.setStatus(client.getStatus());
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("Client e-mail cannot be null or blank");
        }
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BusinessException("Client e-mail has an invalid format");
        }
    }

    private void validateEmailNotRegistered(String email) {
        if (clientRepository.existsByEmail(email)) {
            throw new BusinessException("This e-mail is already registered");
        }
    }

    private void validateEmailNotRegisteredForUpdate(String email, Long clientId) {
        if (clientRepository.existsByEmailAndIdNot(email, clientId)) {
            throw new BusinessException("This e-mail is already registered by another client");
        }
    }


}
