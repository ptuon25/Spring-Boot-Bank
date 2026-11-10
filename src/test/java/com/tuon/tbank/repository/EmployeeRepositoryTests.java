package com.tuon.tbank.repository;

import com.tuon.tbank.entity.person.Employee;
import com.tuon.tbank.enums.person.EmployeeRole;
import com.tuon.tbank.enums.person.EmployeeStatus;
import com.tuon.tbank.repository.person.EmployeeRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class EmployeeRepositoryTests {

    private static final AtomicLong COUNTER = new AtomicLong(8000);
    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final String PASSWORD = "a-strong-password";

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findEmployeeReturnsTrueResults() {
        Employee activeAssociate1 = saveAssociate();
        Employee activeAssociate2 = saveAssociate();
        Employee activeManager1 = saveManager();
        Employee activeManager2 = saveManager();
        Employee activeAdmin1 = saveAdmin();
        Employee activeAdmin2 = saveAdmin();

        Employee deletedAssociate1 = saveDeletedAssociate();
        Employee deletedAssociate2 = saveDeletedAssociate();
        Employee deletedManager1 = saveDeletedManager();
        Employee deletedManager2 = saveDeletedManager();
        Employee deletedAdmin1 = saveDeletedAdmin();
        Employee deletedAdmin2 = saveDeletedAdmin();

        List<Employee> activeEmployees = employeeRepository.findAllByDeletedFalse();
        List<Employee> deletedEmployees = employeeRepository.findAllByDeletedTrue();

        Optional<Employee> activeEmployeeById = employeeRepository.findByIdAndDeletedFalse(activeAssociate1.getId());

        List<Employee> employeesByName = employeeRepository.findByNameContainingIgnoreCaseAndDeletedFalse("repo employee");

        List<Employee> employeesByBirthDateRange = employeeRepository.findByBirthDateBetweenAndDeletedFalse(
                LocalDate.of(1980, 1, 1), LocalDate.of(2000, 12, 31));

        Optional<Employee> activeEmployeeByDocument = employeeRepository.findByDocumentAndDeletedFalse(activeManager1.getDocument());
        Optional<Employee> deletedEmployeeByDocument = employeeRepository.findByDocument(deletedAdmin1.getDocument());

        Optional<Employee> activeEmployeeEmail = employeeRepository.findByEmailAndDeletedFalse(activeAdmin2.getEmail());
        Optional<Employee> deletedEmployeeEmail = employeeRepository.findByEmail(deletedAdmin2.getEmail());

        List<Employee> employeesByRole = employeeRepository.findByRoleAndDeletedFalse(EmployeeRole.MANAGER);

        List<Employee> employeesByAdmissionDateRange = employeeRepository.findByAdmissionDateBetweenAndDeletedFalse(
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        List<Employee> employeesByStatus = employeeRepository.findByStatusAndDeletedFalse(EmployeeStatus.ACTIVE);

        List<Employee> employeesByRoleAndStatus = employeeRepository.findByRoleAndStatusAndDeletedFalse(EmployeeRole.ASSOCIATE, EmployeeStatus.ACTIVE);

        assertAll(
                () -> assertEquals(6, activeEmployees.size()),
                () -> assertEquals(6, deletedEmployees.size()),
                () -> assertTrue(activeEmployeeById.isPresent()),
                () -> assertEquals(6, employeesByName.size()),
                () -> assertEquals(6, employeesByBirthDateRange.size()),
                () -> assertTrue(activeEmployeeByDocument.isPresent()),
                () -> assertTrue(deletedEmployeeByDocument.isPresent()),
                () -> assertTrue(activeEmployeeEmail.isPresent()),
                () -> assertTrue(deletedEmployeeEmail.isPresent()),
                () -> assertEquals(2, employeesByRole.size()),
                () -> assertEquals(6, employeesByAdmissionDateRange.size()),
                () -> assertEquals(6, employeesByStatus.size()),
                () -> assertEquals(2, employeesByRoleAndStatus.size())
        );
    }

    @Test
    void findEmployeeReturnsFalseResults() {

        Employee nonExistentEmployee = saveDeletedAdmin();

        Optional<Employee> nonExistentAdminById = employeeRepository.findByIdAndDeletedFalse(nonExistentEmployee.getId());
        Optional<Employee> nonExistentEmployeeById = employeeRepository.findByIdAndDeletedFalse(9999L);
        List<Employee> nonExistentEmployeesByName = employeeRepository.findByNameContainingIgnoreCaseAndDeletedFalse("Non Existent Employee");
        List<Employee> nonExistentEmployeesByBirthDateRange = employeeRepository.findByBirthDateBetweenAndDeletedFalse(LocalDate.of(1900, 1, 1), LocalDate.of(1900, 12, 31));
        Optional<Employee> nonExistentEmployeeByDocument = employeeRepository.findByDocumentAndDeletedFalse("99999999999");
        Optional<Employee> nonExistentEmployeeByEmail = employeeRepository.findByEmailAndDeletedFalse("non.existent@example.com");
        List<Employee> nonExistentEmployeesByRole = employeeRepository.findByRoleAndDeletedFalse(EmployeeRole.MANAGER);
        List<Employee> nonExistentEmployeesByAdmissionDateRange = employeeRepository.findByAdmissionDateBetweenAndDeletedFalse(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        List<Employee> nonExistentEmployeesByStatus = employeeRepository.findByStatusAndDeletedFalse(EmployeeStatus.ACTIVE);
        List<Employee> nonExistentEmployeesByRoleAndStatus = employeeRepository.findByRoleAndStatusAndDeletedFalse(EmployeeRole.MANAGER, EmployeeStatus.ACTIVE);

        assertAll(
                () -> assertFalse(nonExistentAdminById.isPresent()),
                () -> assertFalse(nonExistentEmployeeById.isPresent()),
                () -> assertTrue(nonExistentEmployeesByName.isEmpty()),
                () -> assertTrue(nonExistentEmployeesByBirthDateRange.isEmpty()),
                () -> assertFalse(nonExistentEmployeeByDocument.isPresent()),
                () -> assertFalse(nonExistentEmployeeByEmail.isPresent()),
                () -> assertTrue(nonExistentEmployeesByRole.isEmpty()),
                () -> assertTrue(nonExistentEmployeesByAdmissionDateRange.isEmpty()),
                () -> assertTrue(nonExistentEmployeesByStatus.isEmpty()),
                () -> assertTrue(nonExistentEmployeesByRoleAndStatus.isEmpty())
        );

    }

    @Test
    void deletedEmployeeDoesNotReturnResults() {

        Employee deletedEmployee1 = saveDeletedAdmin();
        Employee deletedEmployee2 = saveDeletedManager();
        Employee deletedEmployee3 = saveDeletedAssociate();

        Optional<Employee> deletedEmployeeById = employeeRepository.findByIdAndDeletedFalse(deletedEmployee1.getId());
        List<Employee> deletedEmployeesByName = employeeRepository.findByNameContainingIgnoreCaseAndDeletedFalse(deletedEmployee1.getName());
        List<Employee> deletedEmployeesByBirthDateRange = employeeRepository.findByBirthDateBetweenAndDeletedFalse(BIRTH_DATE.minusDays(365), BIRTH_DATE.plusDays(365));
        Optional<Employee> deletedEmployeeByDocument = employeeRepository.findByDocumentAndDeletedFalse(deletedEmployee2.getDocument());
        Optional<Employee> deletedEmployeeByEmail = employeeRepository.findByEmailAndDeletedFalse(deletedEmployee3.getEmail());
        List<Employee> deletedEmployeesByRole = employeeRepository.findByRoleAndDeletedFalse(EmployeeRole.ADMIN);
        List<Employee> deletedEmployeesByAdmissionDateRange = employeeRepository.findByAdmissionDateBetweenAndDeletedFalse(LocalDate.now().minusDays(365), LocalDate.now().plusDays(365));
        List<Employee> deletedEmployeesByStatus = employeeRepository.findByStatusAndDeletedFalse(EmployeeStatus.ACTIVE);
        List<Employee> deletedEmployeesByRoleAndStatus = employeeRepository.findByRoleAndStatusAndDeletedFalse(EmployeeRole.MANAGER, EmployeeStatus.ACTIVE);

        assertAll(
                () -> assertFalse(deletedEmployeeById.isPresent()),
                () -> assertTrue(deletedEmployeesByName.isEmpty()),
                () -> assertTrue(deletedEmployeesByBirthDateRange.isEmpty()),
                () -> assertFalse(deletedEmployeeByDocument.isPresent()),
                () -> assertFalse(deletedEmployeeByEmail.isPresent()),
                () -> assertTrue(deletedEmployeesByRole.isEmpty()),
                () -> assertTrue(deletedEmployeesByAdmissionDateRange.isEmpty()),
                () -> assertTrue(deletedEmployeesByStatus.isEmpty()),
                () -> assertTrue(deletedEmployeesByRoleAndStatus.isEmpty())
        );
    }

    /**
     * Tests the existence checks for employees based on their email and document.
     * This test verifies that the repository correctly identifies existing employees,
     * both active and deleted, based on their email and document.
     * <p>The first assertion checks if an active employee exists by email.</p>
     * <p>The second assertion checks if a deleted employee exists by email.</p>
     * <p>The third assertion checks if an active employee exists by document.</p>
     * <p>The fourth assertion checks if a deleted employee exists by document.</p>
     */
    @Test
    void existsByEmailAndDocumentTests() {

        Employee activeEmployee = saveAssociate();
        Employee deletedEmployee = saveDeletedManager();

        boolean existsActiveByEmail = employeeRepository.existsByEmail(activeEmployee.getEmail());
        boolean existsDeletedByEmail = employeeRepository.existsByEmail(deletedEmployee.getEmail());
        boolean existsActiveByDocument = employeeRepository.existsByDocumentAndDeletedFalse(activeEmployee.getDocument());
        boolean existsDeletedByDocument = employeeRepository.existsByDocumentAndDeletedFalse(deletedEmployee.getDocument());

        assertAll(
                () -> assertTrue(existsActiveByEmail),
                () -> assertTrue(existsDeletedByEmail),
                () -> assertTrue(existsActiveByDocument),
                () -> assertFalse(existsDeletedByDocument)
        );
    }

    /**
     * Tests the existence checks for employees based on their email and document, excluding a specific employee ID.
     * This test verifies that the repository correctly identifies existing employees,
     * both active and deleted, based on their email and document, while excluding a specific employee ID.
     * <p>The first assertion checks if an active employee exists by email, excluding their own ID.</p>
     * <p>The second assertion checks if a deleted employee exists by email, excluding their own ID.</p>
     * <p>The third assertion checks if an active employee exists by document, excluding their own ID.</p>
     * <p>The fourth assertion checks if a deleted employee exists by document, excluding their own ID.</p>
     * <p>The fifth assertion checks if another active employee exists by email, excluding the ID of the first active employee.</p>
     * <p>The sixth assertion checks if another deleted employee exists by email, excluding the ID of the first deleted employee.</p>
     * <p>The seventh assertion checks if another active employee exists by document, excluding the ID of the first active employee.</p>
     * <p>The eighth assertion checks if another deleted employee exists by document, excluding the ID of the first deleted employee.</p>
     */
    @Test
    void existsByEmailAndDocumentAndIdNotTests() {

        Employee activeEmployee = saveAssociate();
        Employee otherActiveEmployee = saveManager();
        Employee deletedEmployee = saveDeletedManager();
        Employee otherDeletedEmployee = saveDeletedAdmin();

        boolean existsActiveByEmailAndIdNot = employeeRepository.existsByEmailAndIdNot(activeEmployee.getEmail(), activeEmployee.getId());
        boolean existsDeletedByEmailAndIdNot = employeeRepository.existsByEmailAndIdNot(deletedEmployee.getEmail(), deletedEmployee.getId());
        boolean existsActiveByDocumentAndIdNot = employeeRepository.existsByDocumentAndDeletedFalseAndIdNot(activeEmployee.getDocument(), activeEmployee.getId());
        boolean existsDeletedByDocumentAndIdNot = employeeRepository.existsByDocumentAndDeletedFalseAndIdNot(deletedEmployee.getDocument(), deletedEmployee.getId());
        boolean existsOtherActiveByEmailAndIdNot = employeeRepository.existsByEmailAndIdNot(activeEmployee.getEmail(), otherActiveEmployee.getId());
        boolean existsOtherDeletedByEmailAndIdNot = employeeRepository.existsByEmailAndIdNot(deletedEmployee.getEmail(), otherDeletedEmployee.getId());
        boolean existsOtherActiveByDocumentAndIdNot = employeeRepository.existsByDocumentAndDeletedFalseAndIdNot(activeEmployee.getDocument(), otherActiveEmployee.getId());
        boolean existsOtherDeletedByDocumentAndIdNot = employeeRepository.existsByDocumentAndDeletedFalseAndIdNot(deletedEmployee.getDocument(), otherDeletedEmployee.getId());

        assertAll(
                () -> assertFalse(existsActiveByEmailAndIdNot),
                () -> assertFalse(existsDeletedByEmailAndIdNot),
                () -> assertFalse(existsActiveByDocumentAndIdNot),
                () -> assertFalse(existsDeletedByDocumentAndIdNot),
                () -> assertTrue(existsOtherActiveByEmailAndIdNot),
                () -> assertTrue(existsOtherDeletedByEmailAndIdNot),
                () -> assertTrue(existsOtherActiveByDocumentAndIdNot),
                () -> assertFalse(existsOtherDeletedByDocumentAndIdNot)
        );
    }

    // AUXILIARY

    private Employee buildAdmin() {
        long n = COUNTER.incrementAndGet();
        return new Employee("Repo Employee " + n, BIRTH_DATE, "80000000" + n,
                "repo.employee" + n + "@test.com", PASSWORD, EmployeeRole.ADMIN,
                LocalDate.now(), EmployeeStatus.ACTIVE, BigDecimal.valueOf(8000.00));
    }

    private Employee buildManager() {
        long n = COUNTER.incrementAndGet();
        return new Employee("Repo Employee " + n, BIRTH_DATE, "80000000" + n,
                "repo.employee" + n + "@test.com", PASSWORD, EmployeeRole.MANAGER,
                LocalDate.now(), EmployeeStatus.ACTIVE, BigDecimal.valueOf(5000.00));
    }

    private Employee buildAssociate() {
        long n = COUNTER.incrementAndGet();
        return new Employee("Repo Employee " + n, BIRTH_DATE, "80000000" + n,
                "repo.employee" + n + "@test.com", PASSWORD, EmployeeRole.ASSOCIATE,
                LocalDate.now(), EmployeeStatus.ACTIVE, BigDecimal.valueOf(3000.00));
    }

    private Employee saveAdmin() {
        Employee employee = buildAdmin();
        return employeeRepository.save(employee);
    }

    private Employee saveManager() {
        Employee employee = buildManager();
        return employeeRepository.save(employee);
    }

    private Employee saveAssociate() {
        Employee employee = buildAssociate();
        return employeeRepository.save(employee);
    }

    private Employee saveDeletedAdmin() {
        Employee employee = buildAdmin();
        employee.setDeleted(true);
        return employeeRepository.save(employee);
    }

    private Employee saveDeletedManager() {
        Employee employee = buildManager();
        employee.setDeleted(true);
        return employeeRepository.save(employee);
    }

    private Employee saveDeletedAssociate() {
        Employee employee = buildAssociate();
        employee.setDeleted(true);
        return employeeRepository.save(employee);
    }


}
