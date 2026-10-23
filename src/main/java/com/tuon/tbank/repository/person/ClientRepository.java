package com.tuon.tbank.repository.person;

import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.enums.person.ClientStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Provides the data access operations for the {@link Client} entity, providing persistence operations
 * and database queries.
 *
 * <p>Extends {@link JpaRepository}, inheriting standard CRUD operations, such as
 * saving, retrieving by ID, and deleting records.</p>
 */
public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Retrieves all clients marked as logically deleted.
     *
     * @return a list of clients marked as logically deleted
     */
    List<Client> findByDeletedTrue();

    /**
     * Retrieves a client by ID, since it's not marked as deleted.
     *
     * @param id the unique client identifier
     * @return an {@link Optional} containing the client if found and not marked as deleted,
     * or an empty {@link Optional} otherwise
     */
    Optional<Client> findByIdAndDeletedFalse(Long id);

    /**
     * Retrieves a client by document, since it's not marked as deleted.
     *
     * @param document the client's identification document
     * @return an {@link Optional} containing the client if found and not marked as deleted,
     * or an empty {@link Optional} otherwise
     */
    Optional<Client> findByDocumentAndDeletedFalse(String document);

    /**
     * Retrieves a client by email address, since it's not marked as deleted.
     *
     * @param email the client's email address
     * @return an {@link Optional} containing the client if found and not marked as deleted,
     * or an empty {@link Optional} otherwise
     */
    Optional<Client> findByEmailAndDeletedFalse(String email);

    /**
     * Retrieves a client by email address.
     *
     * <p>Does not declare a filter for logically excluded records,
     * allowing for the verification of records regardless of their deletion status.</p>
     *
     * @param email the client's email address
     * @return an {@link Optional} containing the client if found,
     * or an empty {@link Optional} otherwise
     */
    Optional<Client> findByEmail(String email);

    /**
     * Retrieves a client by name, since it's not marked as deleted.
     *
     * @param name the client's full name
     * @return an {@link List} containing the client if found and not marked as deleted,
     * or an empty {@link List} otherwise
     */
    List<Client> findByNameAndDeletedFalse(String name);

    /**
     * Retrieves a client by status, since it's not marked as deleted.
     *
     * @param status the client's actual status, detailed in the {@link ClientStatus}
     * @return an {@link List} containing the client if found and not marked as deleted,
     * or an empty {@link List} otherwise
     */
    List<Client> findByStatusAndDeletedFalseOrderByNameAsc(ClientStatus status);

    /**
     * Checks whether a client with the specified email exists.
     *
     * @param email the email address to check
     * @return {@code true} if a client with the email exists, {@code false} otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks whether another client uses the specified email.
     *
     * @param email the email address to check
     * @param id    the identifier of the client to exclude
     * @return {@code true} if another client uses the email, {@code false} otherwise
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Checks whether a client with the specified document exists.
     *
     * @param document the email address to check
     * @return {@code true} if a client with the email exists, {@code false} otherwise
     */
    boolean existsByDocument(String document);

    /**
     * Checks whether another client uses the specified document.
     *
     * @param document the document to check
     * @param id       the identifier of the client to exclude
     * @return {@code true} if another client uses the email, {@code false} otherwise
     */
    boolean existsByDocumentAndIdNot(String document, Long id);
}
