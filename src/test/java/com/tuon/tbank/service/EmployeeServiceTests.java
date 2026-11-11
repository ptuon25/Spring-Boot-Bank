package com.tuon.tbank.service;

import com.tuon.tbank.entity.person.Employee;
import com.tuon.tbank.enums.person.EmployeeRole;
import com.tuon.tbank.enums.person.EmployeeStatus;
import com.tuon.tbank.exception.serviceException.BusinessException;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.person.EmployeeRepository;
import com.tuon.tbank.service.person.EmployeeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class EmployeeServiceTests {

    private static final AtomicLong COUNTER = new AtomicLong(8000);
    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final String PASSWORD = "a-strong-password";

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerEmployeeCreatesValidEmployee() {
        authenticateAsManager();

        String document = "123.456/" + next();
        Employee employee = register("  Employee  ", document, "EMAIL@TBANK.TEST", PASSWORD);
        Employee saved = employeeRepository.findById(employee.getId()).orElseThrow();

        assertAll(
                () -> assertNotNull(saved.getId()),
                () -> assertEquals(EmployeeStatus.ACTIVE, saved.getStatus()),
                () -> assertEquals(LocalDate.now(), saved.getAdmissionDate()),
                () -> assertEquals(document.replaceAll("[.\\-/\\s]", ""), saved.getDocument()),
                () -> assertEquals("email@tbank.test", saved.getEmail()),
                () -> assertTrue(passwordEncoder.matches(PASSWORD, saved.getPasswordHash()))
        );
    }

    @Test
    void registerEmployeeRejectsInvalidBusinessData() {
        authenticateAsManager();

        assertAll(
                () -> assertThrows(BusinessException.class,
                        () -> register("", next(), "valid@email.test", PASSWORD)),
                () -> assertThrows(BusinessException.class,
                        () -> employeeService.registerEmployee("Employee", LocalDate.now().plusDays(1),
                                next(), "future@email.test", PASSWORD, EmployeeRole.MANAGER)),
                () -> assertThrows(BusinessException.class,
                        () -> employeeService.registerEmployee("Employee", LocalDate.now().minusYears(17),
                                next(), "underage@email.test", PASSWORD, EmployeeRole.MANAGER)),
                () -> assertThrows(BusinessException.class,
                        () -> register("Employee", next(), "invalid-email", PASSWORD))
        );
    }

    @Test
    void registerEmployeeRejectsDuplicateEmailOrDocument() {
        authenticateAsManager();
        register("First", "duplicate-document", "duplicate@email.test", PASSWORD);

        assertAll(
                () -> assertThrows(BusinessException.class,
                        () -> register("Second", next(), "duplicate@email.test", PASSWORD)),
                () -> assertThrows(BusinessException.class,
                        () -> register("Second", "duplicate-document", next() + "@email.test", PASSWORD))
        );
    }

    @Test
    void searchAndDeleteEmployeeRespectLogicalDeletion() {
        authenticateAsManager();
        Employee created = register("Searchable", "search-document", "search@email.test", PASSWORD);

        assertEquals(created.getId(), employeeService.getEmployeeByDocument("search document").getId());
        assertEquals(created.getId(), employeeService.getEmployeeByEmail("search@email.test").getId());

        employeeService.deleteEmployee(created.getId());

        assertAll(
                () -> assertThrows(ResourceNotFoundException.class,
                        () -> employeeService.getEmployeeById(created.getId())),
                () -> assertTrue(employeeService.getAllDeletedEmployees().stream()
                        .anyMatch(employee -> employee.getId().equals(created.getId()))),
                () -> assertEquals(created.getId(),
                        employeeService.getEmployeeByEmailIncludingDeleted("search@email.test").getId())
        );
    }

    @Test
    void updateEmployeeChangesEditableDataAndPreservesManagedData() {
        authenticateAsManager();
        Employee created = register("Original", "original-document", "original@email.test", PASSWORD);
        EmployeeStatus status = employeeService.putEmployeeOnLeave(created.getId()).getStatus();
        LocalDate admissionDate = created.getAdmissionDate();
        String passwordHash = employeeRepository.findById(created.getId()).orElseThrow().getPasswordHash();

        created.setName("Updated");
        created.setBirthDate(LocalDate.of(1988, 2, 2));
        created.setDocument("updated-document");
        created.setEmail("UPDATED@EMAIL.TEST");

        Employee saved = employeeService.updateEmployee(created.getId(), created);

        assertAll(
                () -> assertEquals("Updated", saved.getName()),
                () -> assertEquals("updateddocument", saved.getDocument()),
                () -> assertEquals("updated@email.test", saved.getEmail()),
                () -> assertEquals(status, saved.getStatus()),
                () -> assertEquals(admissionDate, saved.getAdmissionDate()),
                () -> assertEquals(passwordHash, saved.getPasswordHash())
        );
    }

    @Test
    void employeeLifecycleAndCompensationOperationsUpdateState() {
        authenticateAsManager();
        Employee employee = register("Managed", "managed-document", "managed@email.test", PASSWORD);

        assertAll(
                () -> assertEquals(new BigDecimal("5000.00"),
                        employeeService.setEmployeeSalary(employee.getId(), new BigDecimal("5000.00")).getSalary()),
                () -> assertEquals(EmployeeRole.ADMIN,
                        employeeService.setEmployeeRole(employee.getId(), EmployeeRole.ADMIN).getRole()),
                () -> assertEquals(EmployeeStatus.ON_LEAVE,
                        employeeService.putEmployeeOnLeave(employee.getId()).getStatus()),
                () -> assertEquals(EmployeeStatus.ACTIVE,
                        employeeService.reactivateEmployee(employee.getId()).getStatus()),
                () -> assertEquals(EmployeeStatus.TERMINATED,
                        employeeService.terminateEmployee(employee.getId()).getStatus())
        );
    }

    @Test
    void employeeLifecycleRejectsInvalidTransitionsAndSalary() {
        authenticateAsManager();
        Employee employee = register("Managed", "invalid-transition-document", "invalid@email.test", PASSWORD);

        assertAll(
                () -> assertThrows(BusinessException.class,
                        () -> employeeService.setEmployeeSalary(employee.getId(), BigDecimal.valueOf(-1))),
                () -> assertThrows(BusinessException.class,
                        () -> employeeService.setEmployeeRole(employee.getId(), EmployeeRole.MANAGER)),
                () -> assertThrows(BusinessException.class,
                        () -> employeeService.reactivateEmployee(employee.getId()))
        );
    }

    @Test
    void employeeCanChangeOwnPasswordAndManagerCanResetIt() {
        authenticateAsManager();
        Employee employee = register("Password", "password-document", "password@email.test", PASSWORD);

        authenticateAsEmployee(employee.getId(), EmployeeRole.ASSOCIATE);
        employeeService.changeOwnPassword(employee.getId(), PASSWORD, "new-password");
        Employee changed = employeeRepository.findById(employee.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("new-password", changed.getPasswordHash()));

        authenticateAsManager();
        employeeService.resetEmployeePassword(employee.getId(), "reset-password");
        Employee reset = employeeRepository.findById(employee.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("reset-password", reset.getPasswordHash()));
    }

    @Test
    void associateCannotManageEmployees() {
        authenticateAsEmployee(1L, EmployeeRole.ASSOCIATE);

        assertThrows(AccessDeniedException.class,
                () -> employeeService.registerEmployee("Unauthorized", BIRTH_DATE,
                        next(), "unauthorized@email.test", PASSWORD, EmployeeRole.ASSOCIATE));
    }

    //AUXILIARY

    private Employee register(String name, String document, String email, String password) {
        return employeeService.registerEmployee(name, BIRTH_DATE, document, email, password, EmployeeRole.MANAGER);
    }

    private String next() {
        return String.valueOf(COUNTER.incrementAndGet());
    }

    private void authenticateAsManager() {
        authenticateAsEmployee(1L, EmployeeRole.MANAGER);
    }

    private void authenticateAsEmployee(long userId, EmployeeRole role) {
        Jwt token = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("identity_type", "EMPLOYEE")
                .claim("user_id", userId)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(token,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}
