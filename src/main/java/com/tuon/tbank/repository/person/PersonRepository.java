package com.tuon.tbank.repository.person;

import com.tuon.tbank.entity.person.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Provides data access operations for the {@link Person} hierarchy.
 */
public interface PersonRepository extends JpaRepository<Person, Long> {

    /**
     * Retrieves a person by ID when the person is not logically deleted.
     *
     * @param id the unique person identifier
     * @return the person if found and not deleted
     */
    Optional<Person> findByIdAndDeletedFalse(Long id);
}
