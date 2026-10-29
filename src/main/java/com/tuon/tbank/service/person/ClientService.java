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

/**
 * Provides business logic for {@link Client} management, including registrations,
 * retrieval, profile updates, account status management, and client deactivation.
 */
@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a client service with the required repository and password encoder.
     *
     * @param clientRepository the repository used to access client data
     * @param passwordEncoder  the encoder used to hash client passwords
     */
    public ClientService(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves all registered clients.
     *
     * @return a list of clients, including those who have been logically deleted
     */
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    /**
     * Retrieves a client by its unique identifier.
     *
     * <p>The search excludes the logically deleted clients.</p>
     *
     * @param id the unique identifier of the client
     * @return the client associated with the specified identifier
     */
    public Client getClientById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("Client ID must be greater than 0");
        }
        Optional<Client> client = clientRepository.findByIdAndDeletedFalse(id);
        return client.orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }

    /**
     * Retrieves all the logically deleted clients.
     *
     * @return a list of all logically deleted clients
     */
    public List<Client> getDeletedClients() {
        return clientRepository.findByDeletedTrue();
    }

    /**
     * Retrieves a list of clients by name.
     *
     * <p>It uses a list because there could be more than one client with the same name.</p>
     *
     * <p>The search excludes the logically deleted clients.</p>
     *
     * @param name the client's full name
     * @return a list of all clients with the searched name
     */
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

    /**
     * Retrieves a list of clients by status.
     *
     * <p>The status are defined by {@link ClientStatus}.</p>
     *
     * <p>The search excludes the logically deleted clients.</p>
     *
     * @param status the client's status
     * @return a list of all clients with the searched status
     */
    public List<Client> getClientByStatus(ClientStatus status) {
        if (status == null) {
            throw new BusinessException("Client status cannot be null");
        }
        return clientRepository.findByStatusAndDeletedFalseOrderByNameAsc(status);
    }

    /**
     * Retrieve a client by email address.
     *
     * <p>The search excludes the logically deleted clients.</p>
     *
     * @param email the client's email address
     * @return the client with the searched email address
     */
    public Client getClientByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("Client e-mail cannot be null or empty");
        }
        Optional<Client> client = clientRepository.findByEmailAndDeletedFalse(email.trim().toLowerCase(Locale.ROOT));
        return client.orElseThrow(() -> new ResourceNotFoundException("Client not found with email: " + email));
    }

    /**
     * Retrieve a client by document.
     *
     * <p>The search excludes the logically deleted clients.</p>
     *
     * @param document the client's identification document
     * @return the client with the searched document
     */
    public Client getClientByDocument(String document) {
        if (document == null || document.isBlank()) {
            throw new BusinessException("Client document cannot be null or empty");
        }
        String normalizedDocument = normalizeDocument(document);
        Optional<Client> client = clientRepository.findByDocumentAndDeletedFalse(normalizedDocument);
        return client.orElseThrow(() -> new ResourceNotFoundException("Client not found with document: " + document));
    }

    /**
     * Creates a client after validating the provided data and persists it
     * in the database. Database constraint violations are translated into
     * a database exception.
     *
     * @param client the client to be created
     * @return the persisted client
     * @throws DatabaseException if a database constraint violation occurs
     */
    public Client createClient(Client client) {

        validateForCreateClient(client);
        try {
            return clientRepository.saveAndFlush(client);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not create client due to a database constraint.");
        }
    }

    /**
     * Registers a new client by validating the required input, normalizing
     * the provided data, encoding the password, and delegating client
     * creation to {@link #createClient(Client)}.
     *
     * @param name      the client's name
     * @param birthDate the client's date of birth
     * @param document  the client's identification document
     * @param email     the client's e-mail address
     * @param password  the client's raw password
     * @return the registered client
     */
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

    /**
     * Updates an existing client's information after validating the provided data
     * and applies the changes to the persisted entity.
     *
     * @param id     the unique identifier of the client to update
     * @param client the client data to apply
     * @return the updated client
     * @throws DatabaseException if a database constraint violation occurs
     */
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

    /**
     * Changes a client's password after verifying the current password
     * and validating the new password.
     *
     * @param id              the unique identifier of the client
     * @param currentPassword the client's current raw password
     * @param newPassword     the new raw password
     * @throws DatabaseException if a database constraint violation occurs
     */
    @PreAuthorize("@clientAuthorization.isOwner(#id, authentication)")
    public void changeOwnPassword(Long id, String currentPassword, String newPassword) {
        Client entity = getClientById(id);
        if (currentPassword == null
                || !passwordEncoder.matches(currentPassword, entity.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }
        validatePassword(newPassword);
        if (passwordEncoder.matches(newPassword, entity.getPasswordHash())) {
            throw new BusinessException("New password must be different from the current password");
        }
        try {
            entity.changePasswordHash(passwordEncoder.encode(newPassword));
            clientRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not change client password due to a database constraint.");
        }
    }

    /**
     * Resets a client's password by validating and encoding the new password
     * before saving the updated password hash.
     *
     * @param id          the unique identifier of the client
     * @param newPassword the new raw password
     * @throws DatabaseException if a database constraint violation occurs
     */
    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public void resetClientPassword(Long id, String newPassword) {
        Client entity = getClientById(id);
        validatePassword(newPassword);
        try {
            entity.changePasswordHash(passwordEncoder.encode(newPassword));
            clientRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not reset client password due to a database constraint.");
        }
    }

    /**
     * Blocks an existing client by changing the client's status to BLOCKED.
     *
     * @param id the unique identifier of the client to block
     * @return the updated client
     * @throws DatabaseException if a database constraint violation occurs
     */
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

    /**
     * Deactivates an existing client by changing the client's status to INACTIVE.
     *
     * <p>Blocked clients cannot be deactivated through this operation.</p>
     *
     * @param id the unique identifier of the client to deactivate
     * @return the updated client
     * @throws DatabaseException if a database constraint violation occurs
     */
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

    /**
     * Reactivates an existing client by changing the client's status to ACTIVE.
     *
     * @param id the unique identifier of the client to reactivate
     * @return the reactivated client
     * @throws DatabaseException if a database constraint violation occurs
     */
    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public Client reactivateClient(Long id) {
        Client entity = getClientById(id);
        if (entity.getStatus() == ClientStatus.ACTIVE) {
            throw new BusinessException("Client is already active");
        }
        try {
            entity.setStatus(ClientStatus.ACTIVE);
            return clientRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not reactivate client due to a database constraint.");
        }
    }

    /**
     * Logically deletes an existing client by marking the client as deleted
     * and saving the change in the database.
     *
     * @param id the unique identifier of the client to delete
     * @throws DatabaseException if a database constraint violation occurs
     */
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

    /**
     * Validates the data required to allow the registration of a new client.
     *
     * @param client the client data to validate
     * @throws BusinessException if a business rule violation occurs
     */
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
        if (client.getBirthDate().isAfter(LocalDate.now().minusYears(18))) {
            throw new BusinessException("Client must be at least 18 years old");
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

    /**
     * Validate the provided data for updating an existing customer,
     * taking into account the customer being modified.
     *
     * @param client the client data to validate
     * @param id     the unique identifier of the client to delete
     * @throws BusinessException if a business rule violation occurs
     */
    private void validateForUpdateClient(Client client, Long id) {
        if (client.getName() == null || client.getName().isBlank()) {
            throw new BusinessException("Client name cannot be null or blank");
        }
        if (client.getBirthDate() == null) {
            throw new BusinessException("Client birthdate cannot be null");
        }
        if (client.getBirthDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Client birthdate cannot be in the future");
        }
        if (client.getBirthDate().isAfter(LocalDate.now().minusYears(18))) {
            throw new BusinessException("Client must be at least 18 years old");
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

    /**
     * Applies the received data to the existing customer's information.
     *
     * <p>It is not possible to change the affiliation date, as it is defined by the system,
     * nor the password, which is changed via {@link #changeOwnPassword(Long, String, String)}
     * or {@link #resetClientPassword(Long, String)}.</p>
     *
     * @param entity the client that already exists in the database, previously retrieved via {@link #getClientById(Long)}.
     * @param client the object received as a parameter, containing the new data that should be applied to the existing client.
     */
    private void updateData(Client entity, Client client) {
        entity.setName(client.getName());
        entity.setBirthDate(client.getBirthDate());
        entity.setDocument(client.getDocument());
        entity.setEmail(client.getEmail());
    }

    /**
     * Verifies whether the provided email address meets the
     * validation criteria defined by the system.
     *
     * @param email the client's email address to be validated
     */
    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("Client e-mail cannot be null or blank");
        }
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BusinessException("Client e-mail has an invalid format");
        }
    }

    /**
     * Check whether the provided email address is not already registered by another customer.
     *
     * @param email the client's email address to be checked
     */
    private void validateEmailNotRegistered(String email) {
        if (clientRepository.existsByEmail(email)) {
            throw new BusinessException("This e-mail is already registered");
        }
    }

    /**
     * Checks the availability of the email address during an update, taking into account the client being updated.
     *
     * @param email    the client's email address to be checked
     * @param clientId the unique identifier of the client
     */
    private void validateEmailNotRegisteredForUpdate(String email, Long clientId) {
        if (clientRepository.existsByEmailAndIdNot(email, clientId)) {
            throw new BusinessException("This e-mail is already registered by another client");
        }
    }

    /**
     * Checks whether the provided document is already registered.
     *
     * @param document the client's document identification to be checked
     */
    private void validateDocumentNotRegistered(String document) {
        if (clientRepository.existsByDocument(document)) {
            throw new BusinessException("This document is already registered");
        }
    }

    /**
     * Checks whether the document is available during an update,
     * taking the current client into account.
     *
     * @param document the client's document identification to be checked
     * @param clientId the unique identifier of the client
     */
    private void validateDocumentNotRegisteredForUpdate(String document, Long clientId) {
        if (clientRepository.existsByDocumentAndIdNot(document, clientId)) {
            throw new BusinessException("This document is already registered by another client");
        }
    }

    /**
     * Checks whether the password meets the requirements defined by the system.
     *
     * @param password the password to be checked
     */
    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException("Client password cannot be null or blank");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("Client password exceeds BCrypt's supported input length");
        }
    }

    /**
     * Standardizes the document by removing formatting characters,
     * in accordance with the method's implementation.
     *
     * @param document the client's document identification
     * @return the standardized document,
     * with no periods or hyphens, and in lowercase letters
     */
    private String normalizeDocument(String document) {
        return document.replaceAll("[.\\-/\\s]", "");
    }


}
