package com.tuon.tbank.repository.person;

import com.tuon.tbank.entity.person.Employee;
import com.tuon.tbank.enums.person.EmployeeRole;
import com.tuon.tbank.enums.person.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Provides the data access operations for the {@link Employee} entity, providing persistence operations
 * and database queries.
 *
 * <p>Extends {@link JpaRepository}, inheriting standard CRUD operations, such as saving, updating, and deleting entities.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Retrieves a list of employees that are not marked as deleted.
     *
     * @return a list of employees that are not marked as deleted
     */
    List<Employee> findAllByDeletedFalse();

    /**
     * Retrieves a list of employees that have been marked as deleted and match the specified document.
     *
     * @return a list of employees that are marked as deleted and match the specified document
     */
    List<Employee> findByDeletedTrue();

    /**
     * Retrieves an employee by their unique ID, ensuring they are not marked as deleted.
     *
     * @param id the unique ID of the employee
     * @return an {@link Optional} containing the employee if found and not deleted, or an empty {@link Optional} otherwise
     */
    Optional<Employee> findByIdAndDeletedFalse(Long id);

    /**
     * Retrieves a list of employees whose birthdays fall within the specified range and are not marked as deleted.
     *
     * @param startDate the start date of the birthdate range
     * @param endDate   the end date of the birthdate range
     * @return a list of employees whose birthdays fall within the specified range and are not marked as deleted
     */
    List<Employee> findByBirthDateBetweenAndDeletedFalse(LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves an employee by their unique document identifier, ensuring they are not marked as deleted.
     *
     * @param document the unique document identifier of the employee
     * @return an {@link Optional} containing the employee if found and not deleted, or an empty {@link Optional} otherwise
     */
    Optional<Employee> findByDocumentAndDeletedFalse(String document);

    /**
     * Retrieves an employee by their unique document identifier.
     *
     * <p>This method will return an employee if they exist and even if they are marked as deleted.</p>
     *
     * @param document the unique document identifier of the employee
     * @return an {@link Optional} containing the employee if found, or an empty {@link Optional} otherwise
     */
    Optional<Employee> findByDocument(String document);

    /**
     * Retrieves an employee by their email address, ensuring they are not marked as deleted.
     *
     * @param email the email address of the employee
     * @return an {@link Optional} containing the employee if found and not deleted, or an empty {@link Optional} otherwise
     */
    Optional<Employee> findByEmailAndDeletedFalse(String email);

    /**
     * Retrieves an employee by their email address.
     *
     * <p>This method will return an employee if they exist and even if they are marked as deleted.</p>
     *
     * @param email the email address of the employee
     * @return an {@link Optional} containing the employee if found, or an empty {@link Optional} otherwise
     */
    Optional<Employee> findByEmail(String email);

    /**
     * Retrieves a list of employees with the specified role, ensuring they are not marked as deleted.
     *
     * @param role the role of the employees to retrieve
     * @return a list of employees with the specified role and not marked as deleted
     */
    List<Employee> findByRoleAndDeletedFalse(EmployeeRole role);

    /**
     * Retrieves a list of employees whose admission dates fall within the specified range and are not marked as deleted.
     *
     * @param startDate the start date of the admission date range
     * @param endDate   the end date of the admission date range
     * @return a list of employees whose admission dates fall within the specified range and are not marked as deleted
     */
    List<Employee> findByAdmissionDateBetweenAndDeletedFalse(LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves a list of employees with the specified status, ensuring they are not marked as deleted.
     *
     * @param status the status of the employees to retrieve
     * @return a list of employees with the specified status and not marked as deleted
     */
    List<Employee> findByStatusAndDeletedFalse(EmployeeStatus status);

    /**
     * Retrieves a list of employees with the specified role and status, ensuring they are not marked as deleted.
     *
     * @param role   the role of the employees to retrieve
     * @param status the status of the employees to retrieve
     * @return a list of employees with the specified role and status and not marked as deleted
     */
    List<Employee> findByRoleAndStatusAndDeletedFalse(EmployeeRole role, EmployeeStatus status);

    /**
     * Retrieves an employee by their unique document identifier.
     *
     * @param document the unique document identifier of the employee
     * @return an {@link Optional} containing the employee if found, or an empty {@link Optional} otherwise
     */
    boolean existsByDocumentAndDeletedFalse(String document);

    /**
     * Checks if an employee exists with the given document, excluding the employee with the specified ID.
     *
     * @param document the unique document identifier of the employee
     * @param id       the ID of the employee to exclude from the check
     * @return true if an employee exists with the given document and a different ID, false otherwise
     */
    boolean existsByDocumentAndDeletedFalseAndIdNot(String document, Long id);

    /**
     * Checks if an employee exists with the given email address.
     *
     * @param email the email address of the employee
     * @return true if an employee exists with the given email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if an employee exists with the given email address, excluding the employee with the specified ID.
     *
     * @param email the email address of the employee
     * @param id    the ID of the employee to exclude from the check
     * @return true if an employee exists with the given email and a different ID, false otherwise
     */
    boolean existsByEmailAndIdNot(String email, Long id);
}
