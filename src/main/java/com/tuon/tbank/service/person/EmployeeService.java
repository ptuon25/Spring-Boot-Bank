package com.tuon.tbank.service.person;

import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.entity.person.Employee;
import com.tuon.tbank.enums.person.EmployeeRole;
import com.tuon.tbank.enums.person.EmployeeStatus;
import com.tuon.tbank.exception.serviceException.BusinessException;
import com.tuon.tbank.exception.serviceException.DatabaseException;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.person.EmployeeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Service class for managing employees.
 *
 * <p>This service provides methods to retrieve employee information from the database.</p>
 */
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves all employees from the database.
     *
     * <p>This method fetches all employee records from the database and returns them as a list.</p>
     *
     * @return a list of all employees, including those who have been logically deleted
     */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * Retrieves all employees that are marked as deleted from the database.
     *
     * <p>This method fetches all employee records that have been logically deleted and returns them as a list.</p>
     *
     * @return a list of all employees that are marked as deleted
     */
    public List<Employee> getAllDeletedEmployees() {
        return employeeRepository.findByDeletedTrue();
    }

    /**
     * Retrieves all employees that are not marked as deleted from the database.
     *
     * <p>This method fetches all employee records that are active and returns them as a list.</p>
     *
     * @return a list of all employees that are not marked as deleted
     */
    public List<Employee> getAllNonDeletedEmployees() {
        return employeeRepository.findAllByDeletedFalse();
    }

    /**
     * Retrieves an employee by their unique ID.
     *
     * <p>This method fetches an employee record from the database based on the provided ID.</p>
     *
     * @param id the unique ID of the employee
     * @return the employee with the specified ID
     * @throws ResourceNotFoundException if the employee is not found
     * @throws BusinessException         if the employee is not found or if the ID is invalid
     */
    public Employee getEmployeeById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("Invalid employee ID");
        }
        Optional<Employee> employee = employeeRepository.findByIdAndDeletedFalse(id);
        return employee.orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
    }

    /**
     * Retrieves employees by a date range.
     *
     * <p>This method fetches employee records from the database based on a date range.</p>
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return a list of employees within the specified date range
     * @throws BusinessException if the date range is invalid
     */
    public List<Employee> getEmployeesByBirthDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date");
        }
        return employeeRepository.findByBirthDateBetweenAndDeletedFalse(startDate, endDate);
    }

    /**
     * Retrieves an employee by their unique document identifier.
     *
     * <p>This method fetches an employee record from the database based on the provided document identifier.</p>
     *
     * @param document the unique document identifier of the employee
     * @return the employee with the specified document
     * @throws ResourceNotFoundException if the employee is not found
     * @throws BusinessException         if the document is null or blank
     */
    public Employee getEmployeeByDocument(String document) {
        if (document == null || document.isBlank()) {
            throw new BusinessException("Document cannot be null or blank");
        }
        String normalizedDoc = normalizeDocument(document);
        Optional<Employee> employee = employeeRepository.findByDocumentAndDeletedFalse(normalizedDoc);
        return employee.orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    /**
     * Retrieves an employee by their email address, excluding deleted employees.
     *
     * <p>This method fetches an employee record from the database based on the provided email address.</p>
     *
     * @param email the email address of the employee
     * @return the employee with the specified email
     * @throws ResourceNotFoundException if the employee is not found
     * @throws BusinessException         if the email is null or blank
     */
    public Employee getEmployeeByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("Email cannot be null or blank");
        }
        validateEmail(email);
        Optional<Employee> employee = employeeRepository.findByEmailAndDeletedFalse(email);
        return employee.orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    /**
     * Retrieves an employee by their email address, including deleted employees.
     *
     * <p>This method fetches an employee record from the database based on the provided email address.</p>
     *
     * @param email the email address of the employee
     * @return the employee with the specified email
     * @throws ResourceNotFoundException if the employee is not found
     * @throws BusinessException         if the email is null or blank
     */
    public Employee getEmployeeByEmailIncludingDeleted(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("Email cannot be null or blank");
        }
        validateEmail(email);
        Optional<Employee> employee = employeeRepository.findByEmail(email.toLowerCase(Locale.ROOT));
        return employee.orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    /**
     * Retrieves a list of employees by their role, excluding deleted employees.
     *
     * <p>This method fetches a list of employee records from the database based on the provided role.</p>
     *
     * @param role the role of the employees
     * @return the list of employees with the specified role
     * @throws BusinessException if the role is null
     */
    public List<Employee> getEmployeesByRole(EmployeeRole role) {
        if (role == null) {
            throw new BusinessException("Employee role cannot be null");
        }
        return employeeRepository.findByRoleAndDeletedFalse(role);
    }

    /**
     * Retrieves a list of employees by their admission date range, excluding deleted employees.
     *
     * <p>This method fetches a list of employee records from the database based on the provided date range.</p>
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return the list of employees with admission dates in the specified range
     * @throws BusinessException if the start date or end date is null, or if the start date is after the end date
     */
    public List<Employee> getEmployeeByAdmissionDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date");
        }
        return employeeRepository.findByAdmissionDateBetweenAndDeletedFalse(startDate, endDate);
    }

    /**
     * Retrieves a list of employees by their status, excluding deleted employees.
     *
     * <p>This method fetches a list of employee records from the database based on the provided status.</p>
     *
     * @param status the status of the employees
     * @return the list of employees with the specified status
     * @throws BusinessException if the status is null
     */
    public List<Employee> getEmployeesByStatus(EmployeeStatus status) {
        if (status == null) {
            throw new BusinessException("Employee status cannot be null");
        }
        return employeeRepository.findByStatusAndDeletedFalse(status);
    }

    /**
     * Retrieves a list of employees by their role and status, excluding deleted employees.
     *
     * <p>This method fetches a list of employee records from the database based on the provided role and status.</p>
     *
     * @param role   the role of the employees
     * @param status the status of the employees
     * @return the list of employees with the specified role and status
     * @throws BusinessException if the role or status is null
     */
    public List<Employee> getEmployeesByRoleAndStatus(EmployeeRole role, EmployeeStatus status) {
        if (role == null) {
            throw new BusinessException("Employee role cannot be null");
        }
        if (status == null) {
            throw new BusinessException("Employee status cannot be null");
        }
        return employeeRepository.findByRoleAndStatusAndDeletedFalse(role, status);
    }

    /**
     * Creates a new employee in the database.
     *
     * <p>This method validates the provided employee data and saves it to the database.</p>
     *
     * @param employee the employee to create
     * @return the created employee
     * @throws BusinessException if the employee data is invalid
     * @throws DatabaseException if there is a database error during the update operation
     */
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public Employee createEmployee(Employee employee) {
        validateEmployeeForCreate(employee);
        try {
            return employeeRepository.saveAndFlush(employee);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Failed to create employee: " + e.getMessage());
        }
    }

    /**
     * Registers a new employee in the database.
     *
     * <p>This method validates the provided employee data, encodes the password, and saves it to the database.</p>
     *
     * @param name      the name of the employee
     * @param birthDate the birthdate of the employee
     * @param document  the document identifier of the employee
     * @param email     the email address of the employee
     * @param password  the password of the employee
     * @param role      the role of the employee
     * @return the registered employee
     * @throws BusinessException if any of the provided data is invalid
     */
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public Employee registerEmployee(String name, LocalDate birthDate, String document,
                                     String email, String password, EmployeeRole role) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Employee name cannot be null or blank");
        }
        if (birthDate == null) {
            throw new BusinessException("Employee birth date cannot be null");
        }
        if (document == null || document.isBlank()) {
            throw new BusinessException("Employee document cannot be null or blank");
        }
        String normalizedDoc = normalizeDocument(document);
        if (normalizedDoc.isBlank()) {
            throw new BusinessException("Employee document cannot be blank");
        }
        validateEmail(email);
        if (password == null || password.isBlank()) {
            throw new BusinessException("Employee password cannot be null or blank");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("Client password exceeds BCrypt's supported input length");
        }
        if (role == null) {
            throw new BusinessException("Employee role cannot be null");
        }
        Employee employee = new Employee(name, birthDate, normalizedDoc, email.trim().toLowerCase(Locale.ROOT),
                passwordEncoder.encode(password), role, LocalDate.now(), EmployeeStatus.ACTIVE, null);
        return createEmployee(employee);
    }

    /**
     * Updates an existing employee in the database.
     *
     * <p>This method retrieves the employee by their ID, validates the provided employee data, and updates the existing employee record in the database.</p>
     *
     * @param id       the ID of the employee to update
     * @param employee the updated employee data
     * @return the updated employee
     * @throws BusinessException if the employee data is invalid
     * @throws DatabaseException if there is a database error during the update operation
     */
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public Employee updateEmployee(Long id, Employee employee) {
        if (employee == null) {
            throw new BusinessException("Employee cannot be null");
        }
        Employee entity = getEmployeeById(id);
        validateEmployeeForUpdate(employee, id);
        updateData(entity, employee);
        try {
            return employeeRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Failed to update employee: " + e.getMessage());
        }
    }

    /**
     * Sets the salary of an employee.
     *
     * <p>This method retrieves the employee by their ID, sets the provided salary, and saves the updated employee to the database.</p>
     *
     * @param id     the ID of the employee
     * @param salary the new salary to set
     * @return the updated employee with the new salary
     * @throws BusinessException if the salary is null or negative, or if there is a database error
     */
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public Employee setEmployeeSalary(Long id, BigDecimal salary) {
        Employee employee = getEmployeeById(id);
        if (employee == null) {
            throw new BusinessException("Employee cannot be null");
        }
        if(employee.getId() == null){
            throw new BusinessException("Employee ID cannot be null");
        }
        if (salary == null || salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Salary cannot be null or negative");
        }
        employee.setSalary(salary);
        try {
            return employeeRepository.saveAndFlush(employee);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Failed to set employee salary: " + e.getMessage());
        }
    }

    /**
     * Sets the role of an employee.
     *
     * <p>This method retrieves the employee by their ID, sets the provided role, and saves the updated employee to the database.</p>
     *
     * @param employee the employee to update the role for
     * @param entity   the updated employee data
     * @return the updated employee with the new role
     * @throws BusinessException if the role is null or if there is a database error
     */
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public Employee setEmployeeRole(Employee employee, Employee entity) {
        getEmployeeById(employee.getId());
        if(employee.getRole() == entity.getRole()){
            throw new BusinessException("Employee role cannot be equal to the current role");
        }
        employee.setRole(entity.getRole());
        try {
            return employeeRepository.saveAndFlush(employee);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Failed to set employee role: " + e.getMessage());
        }
    }

    /**
     * Puts an employee on leave by updating their status.
     *
     * <p>This method retrieves the employee by their ID, checks if they are terminated, and updates their status to the provided status.</p>
     *
     * @param employee the employee to update the status for
     * @param entity   the updated employee data
     * @return the updated employee with the new status
     * @throws BusinessException if the employee is terminated or if there is a database error
     */
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public Employee putEmployeeOnLeave(Employee employee, Employee entity){
        getEmployeeById(employee.getId());
        if(entity.getStatus() == EmployeeStatus.TERMINATED){
            throw new BusinessException("Cannot put employee on leave if they are terminated");
        }
        employee.setStatus(entity.getStatus());
        try {
            return employeeRepository.saveAndFlush(employee);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Failed to set employee status: " + e.getMessage());
        }
    }

    /**
     * Terminates an employee by updating their status.
     *
     * <p>This method retrieves the employee by their ID, checks if they are terminated, and updates their status to the provided status.</p>
     *
     * @param employee the employee to update the status for
     * @param entity   the updated employee data
     * @return the updated employee with the new status
     * @throws BusinessException if the employee is not terminated or if there is a database error
     */
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public Employee terminateEmployee(Employee employee, Employee entity){
        getEmployeeById(employee.getId());
        if(entity.getStatus() == EmployeeStatus.TERMINATED){
            throw new BusinessException("The employee is already terminated");
        }
        if(entity.getStatus() == EmployeeStatus.ON_LEAVE){
            throw new BusinessException("Cannot terminate an employee that is on leave");
        }
        employee.setStatus(entity.getStatus());
        try {
            return employeeRepository.saveAndFlush(employee);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Failed to set employee status: " + e.getMessage());
        }
    }

    /**
     * Deletes an employee by marking them as deleted.
     *
     * <p>This method retrieves the employee by their ID, marks them as deleted, and saves the updated employee to the database.</p>
     *
     * @param id the ID of the employee to delete
     * @return the deleted employee
     * @throws BusinessException if there is a database error during the delete operation
     */
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public Employee deleteEmployee(Long id){
        Employee employee = getEmployeeById(id);
        try {
            employee.setDeleted(true);
            return employeeRepository.saveAndFlush(employee);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Failed to delete employee: " + e.getMessage());
        }
    }

    /**
     * Reactivates an employee by updating their status.
     *
     * <p>This method retrieves the employee by their ID, checks if they are active, and updates their status to the provided status.</p>
     *
     * @param employee the employee to update the status for
     * @param entity   the updated employee data
     * @return the updated employee with the new status
     * @throws BusinessException if the employee is not active or if there is a database error
     */
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public Employee reactivateEmployee(Employee employee, Employee entity){
        getEmployeeById(employee.getId());
        if(entity.getStatus() == EmployeeStatus.ACTIVE){
            throw new BusinessException("The employee is already active");
        }
        employee.setStatus(entity.getStatus());
        try {
            return employeeRepository.saveAndFlush(employee);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Failed to set employee status: " + e.getMessage());
        }
    }

    /**
     * Changes the password of an employee.
     *
     * <p>This method retrieves the employee by their ID, validates the current password, and updates the password to the new one.</p>
     *
     * @param id              the ID of the employee
     * @param currentPassword the current password of the employee
     * @param newPassword     the new password to set
     * @return the updated employee with the new password
     * @throws BusinessException if the current password is incorrect, if the new password is invalid, or if there is a database error
     */
    @PreAuthorize("@employeeAuthorization.isOwner(#id, authentication)")
    public Employee changeOwnPassword(Long id, String currentPassword, String newPassword) {
        Employee entity = getEmployeeById(id);
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
            return employeeRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Failed to change employee password: " + e.getMessage());
        }
    }

    /**
     * Resets the password of an employee.
     *
     * <p>This method retrieves the employee by their ID, validates the new password, and updates the password to the new one.</p>
     *
     * @param id          the ID of the employee
     * @param newPassword the new password to set
     * @throws BusinessException if the new password is invalid or if there is a database error
     */
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public void resetEmployeePassword(Long id, String newPassword) {
        Employee entity = getEmployeeById(id);
        validatePassword(newPassword);
        try {
            entity.changePasswordHash(passwordEncoder.encode(newPassword));
            employeeRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Failed to reset employee password: " + e.getMessage());
        }
    }

    /**
     * Validates the employee data before creating a new employee.
     *
     * @param employee the employee to validate
     * @throws BusinessException if the employee data is invalid
     */
    private void validateEmployeeForCreate(Employee employee) {
        if (employee == null) {
            throw new BusinessException("Employee cannot be null");
        }
        if (employee.getName() == null || employee.getName().isBlank()) {
            throw new BusinessException("Employee name cannot be null or blank");
        }
        if (employee.getBirthDate() == null) {
            throw new BusinessException("Employee birth date cannot be null");
        }
        if (employee.getBirthDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Employee birth date cannot be in the future");
        }
        String normalizedDoc = normalizeDocument(employee.getDocument());
        if (normalizedDoc.isBlank()) {
            throw new BusinessException("Employee document cannot be null or blank");
        }
        validateDocumentNotRegistered(normalizedDoc);
        validateEmail(employee.getEmail());
        validateEmailNotRegistered(employee.getEmail());
        if (employee.getPasswordHash() == null || employee.getPasswordHash().isBlank()) {
            throw new BusinessException("Employee password cannot be null or blank");
        }
        if (employee.getRole() == null) {
            throw new BusinessException("Employee role cannot be null");
        }
        validateBirthdateWithRole(employee.getBirthDate(), employee.getRole());
        if (employee.getAdmissionDate() == null) {
            throw new BusinessException("Employee admission date cannot be null");
        }
        if (employee.getAdmissionDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Employee admission date cannot be in the future");
        }
        if (employee.getStatus() == null) {
            throw new BusinessException("Employee status cannot be null");
        }
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new BusinessException("Employee cannot be created without ACTIVE status");
        }
    }

    /**
     * Validates the employee data before updating an existing employee.
     *
     * @param employee the employee to validate
     * @param id       the ID of the employee being updated
     * @throws BusinessException if the employee data is invalid
     */
    private void validateEmployeeForUpdate(Employee employee, Long id) {
        if (employee == null) {
            throw new BusinessException("Employee cannot be null");
        }
        if (employee.getId() == null || !employee.getId().equals(id)) {
            throw new BusinessException("Employee ID mismatch");
        }
        if (employee.getName() == null || employee.getName().isBlank()) {
            throw new BusinessException("Employee name cannot be null or blank");
        }
        if (employee.getBirthDate() == null) {
            throw new BusinessException("Employee birth date cannot be null");
        }
        if (employee.getBirthDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Employee birth date cannot be in the future");
        }
        String normalizedDoc = normalizeDocument(employee.getDocument());
        if (normalizedDoc.isBlank()) {
            throw new BusinessException("Employee document cannot be null or blank");
        }
        validateDocumentNotRegisteredForUpdate(normalizedDoc, id);
        validateEmail(employee.getEmail());
        validateEmailNotRegisteredForUpdate(employee.getEmail(), id);
        if (employee.getRole() == null) {
            throw new BusinessException("Employee role cannot be null");
        }
        validateBirthdateWithRole(employee.getBirthDate(), employee.getRole());
        if (employee.getAdmissionDate() == null) {
            throw new BusinessException("Employee admission date cannot be null");
        }
        if (employee.getAdmissionDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Employee admission date cannot be in the future");
        }
        if (employee.getStatus() == null) {
            throw new BusinessException("Employee status cannot be null");
        }
    }

    /**
     * Updates the data of an existing employee entity with the provided employee information.
     *
     * @param entity   the existing employee entity to be updated
     * @param employee the employee containing the new data
     */
    private void updateData(Employee entity, Employee employee) {
        entity.setName(employee.getName());
        entity.setBirthDate(employee.getBirthDate());
        entity.setDocument(normalizeDocument(employee.getDocument()));
        entity.setEmail(employee.getEmail().trim().toLowerCase(Locale.ROOT));
    }

    /**
     * Validates that the provided document is not already registered for another employee.
     *
     * @param document the document to validate
     * @throws BusinessException if the document is already registered for another employee
     */
    private void validateDocumentNotRegistered(String document) {
        if (employeeRepository.existsByDocumentAndDeletedFalse(normalizeDocument(document))) {
            throw new BusinessException("Document already registered");
        }
    }

    /**
     * Validates that the provided email is not already registered for another employee.
     *
     * @param email the email to validate
     * @throws BusinessException if the email is already registered for another employee
     */
    private void validateEmailNotRegistered(String email) {
        if (employeeRepository.existsByEmail(email)) {
            throw new BusinessException("Email already registered");
        }
    }

    /**
     * Validates that the provided document is not already registered for another employee during an update operation.
     *
     * @param document the document to validate
     * @param id       the ID of the employee being updated
     * @throws BusinessException if the document is already registered for another employee
     */
    private void validateDocumentNotRegisteredForUpdate(String document, Long id) {
        if (employeeRepository.existsByDocumentAndDeletedFalseAndIdNot(normalizeDocument(document), id)) {
            throw new BusinessException("Document already registered");
        }
    }

    /**
     * Validates that the provided email is not already registered for another employee during an update operation.
     *
     * @param email the email to validate
     * @param id    the ID of the employee being updated
     * @throws BusinessException if the email is already registered for another employee
     */
    private void validateEmailNotRegisteredForUpdate(String email, Long id) {
        if (employeeRepository.existsByEmailAndIdNot(email, id)) {
            throw new BusinessException("Email already registered");
        }
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
     * Validates the employee's birthdate based on their role.
     *
     * <p>This method checks if the employee's birthdate meets the age requirements for their role.</p>
     *
     * <p>If the employee is an ADMIN or MANAGER, they must be at least 18 years old.</p>
     * <p>If the employee is an ASSOCIATE, they must be at least 16 years old.</p>
     *
     * @param birthDate the birthdate of the employee
     * @param role      the role of the employee
     * @throws BusinessException if the birthdate does not meet the age requirements for the specified role
     */
    private void validateBirthdateWithRole(LocalDate birthDate, EmployeeRole role) {
        if (birthDate == null) {
            throw new BusinessException("Employee birth date cannot be null");
        }
        if (role == EmployeeRole.ADMIN || role == EmployeeRole.MANAGER) {
            if (birthDate.isAfter(LocalDate.now().minusYears(18))) {
                throw new BusinessException("Employee must be at least 18 years old for ADMIN or MANAGER role");
            }
        } else if (role == EmployeeRole.ASSOCIATE) {
            if (birthDate.isAfter(LocalDate.now().minusYears(16))) {
                throw new BusinessException("Employee must be at least 16 years old for ASSOCIATE role");
            }
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
