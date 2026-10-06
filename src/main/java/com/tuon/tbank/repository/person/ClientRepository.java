package com.tuon.tbank.repository.person;

import com.tuon.tbank.entity.person.Client;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for clients.
 */
public interface ClientRepository extends JpaRepository<Client, Long> {
}
