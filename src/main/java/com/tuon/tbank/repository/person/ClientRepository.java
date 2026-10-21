package com.tuon.tbank.repository.person;

import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.enums.person.ClientStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Provides database access operations for clients.
 */
public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByDeletedTrue();

    Optional<Client> findByIdAndDeletedFalse(Long id);

    Optional<Client> findByDocumentAndDeletedFalse(String document);

    Optional<Client> findByEmailAndDeletedFalse(String email);

    Optional<Client> findByEmail(String email);

    List<Client> findByNameAndDeletedFalse(String name);

    List<Client> findByStatusAndDeletedFalseOrderByNameAsc(ClientStatus status);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByDocument(String document);

    boolean existsByDocumentAndIdNot(String document, Long id);
}
