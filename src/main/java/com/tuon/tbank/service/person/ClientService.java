package com.tuon.tbank.service.person;

import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.enums.person.ClientStatus;
import com.tuon.tbank.exception.serviceException.BusinessException;
import com.tuon.tbank.exception.serviceException.DatabaseException;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.person.ClientRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
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
        if (id == null || id <= 0) {
            throw new BusinessException("Client ID must be greater than 0");
        }
        Optional<Client> client = clientRepository.findByIdAndDeletedFalse(id);
        return client.orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }

    public List<Client> getDeletedClients() {
        return clientRepository.findByDeletedTrue();
    }

    public List<Client> getClientByName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Client name cannot be null or blank");
        }
        List<Client> clients = clientRepository.findByNameAndDeletedFalse(name.trim());
        if (clients.isEmpty()) {
            throw new ResourceNotFoundException("No clients found with name: " + name.trim());
        }
        return clients;
    }

    public List<Client> getClientByStatus(ClientStatus status) {
        if (status == null) {
            throw new BusinessException("Client status cannot be null");
        }
        return clientRepository.findByStatusAndDeletedFalseOrderByNameAsc(status);
    }

    public Client getClientByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("Client e-mail cannot be null or empty");
        }
        Optional<Client> client = clientRepository.findByEmailAndDeletedFalse(email.trim().toLowerCase(Locale.ROOT));
        return client.orElseThrow(() -> new ResourceNotFoundException("Client not found with email: " + email));
    }

    public Client getClientByDocument(String document) {
        if (document == null || document.isBlank()) {
            throw new BusinessException("Client document cannot be null or empty");
        }
        String normalizedDocument = normalizeDocument(document);
        Optional<Client> client = clientRepository.findByDocumentAndDeletedFalse(normalizedDocument);
        return client.orElseThrow(() -> new ResourceNotFoundException("Client not found with document: " + document));
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
        if (password == null || password.isBlank()) {
            throw new BusinessException("Client password cannot be null or blank");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("Client password exceeds BCrypt's supported input length");
        }
        if (email == null || email.isBlank()) {
            throw new BusinessException("Client e-mail cannot be null or blank");
        }
        if (document == null || document.isBlank()) {
            throw new BusinessException("Client document cannot be null or blank");
        }
        validateEmail(email);
        String normalizedDocument = normalizeDocument(document);
        if (normalizedDocument.isBlank()) {
            throw new BusinessException("Client document cannot be blank");
        }
        Client client = new Client(name, birthDate, normalizedDocument, email.trim().toLowerCase(Locale.ROOT),
                passwordEncoder.encode(password), LocalDate.now(), ClientStatus.ACTIVE);
        return createClient(client);
    }

    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
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

    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public Client blockClient(Long id) {
        Client entity = getClientById(id);
        if (entity.getStatus() == ClientStatus.BLOCKED) {
            throw new BusinessException("Client is already blocked");
        }
        try {
            entity.setStatus(ClientStatus.BLOCKED);
            return clientRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not block client due to a database constraint.");
        }
    }

    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public Client deactivateClient(Long id) {
        Client entity = getClientById(id);
        if (entity.getStatus() == ClientStatus.INACTIVE) {
            throw new BusinessException("Client is already inactive");
        }
        if (entity.getStatus() == ClientStatus.BLOCKED) {
            throw new BusinessException("Cannot deactivate a blocked client");
        }
        try {
            entity.setStatus(ClientStatus.INACTIVE);
            return clientRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not deactivate client due to a database constraint.");
        }
    }

    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public Client reactivateClient(Long id) {
        Client entity = getClientById(id);
        if (entity.getStatus() == ClientStatus.ACTIVE) {
            throw new BusinessException("Client is already active");
        }
        try{
            entity.setStatus(ClientStatus.ACTIVE);
            return clientRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e){
            throw new DatabaseException("Could not reactivate client due to a database constraint.");
        }
    }

    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public void deleteClient(Long id) {
        Client client = getClientById(id);
        try {
            client.setDeleted(true);
            clientRepository.saveAndFlush(client);
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
        String normalizedDocument = normalizeDocument(client.getDocument());
        if (normalizedDocument.isBlank()) {
            throw new BusinessException("Client document cannot be blank");
        }
        validateDocumentNotRegistered(normalizedDocument);
        client.setDocument(normalizedDocument);
        validateEmail(client.getEmail());
        String normalizedEmail = client.getEmail().trim().toLowerCase(Locale.ROOT);
        validateEmail(normalizedEmail);
        validateEmailNotRegistered(normalizedEmail);
        client.setEmail(normalizedEmail);
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
        String normalizedDocument = normalizeDocument(client.getDocument());
        if (normalizedDocument.isBlank()) {
            throw new BusinessException("Client document cannot be blank");
        }
        validateDocumentNotRegisteredForUpdate(normalizedDocument, id);
        client.setDocument(normalizedDocument);
        if (client.getEmail() == null) {
            throw new BusinessException("Client e-mail cannot be null");
        }
        String normalizedEmail = client.getEmail().trim().toLowerCase(Locale.ROOT);
        validateEmail(normalizedEmail);
        validateEmailNotRegisteredForUpdate(normalizedEmail, id);
        client.setEmail(normalizedEmail);
    }

    private void updateData(Client entity, Client client) {
        entity.setName(client.getName());
        entity.setBirthDate(client.getBirthDate());
        entity.setDocument(client.getDocument());
        entity.setEmail(client.getEmail());
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

    private void validateDocumentNotRegistered(String document) {
        if (clientRepository.existsByDocument(document)) {
            throw new BusinessException("This document is already registered");
        }
    }

    private void validateDocumentNotRegisteredForUpdate(String document, Long clientId) {
        if (clientRepository.existsByDocumentAndIdNot(document, clientId)) {
            throw new BusinessException("This document is already registered by another client");
        }
    }

    private String normalizeDocument(String document) {
        return document.replaceAll("[.\\-/\\s]", "");
    }


}
