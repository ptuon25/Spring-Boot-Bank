package com.tuon.tbank.controller.person;

import com.tuon.tbank.dto.auth.EmployeeRegistrationRequest;
import com.tuon.tbank.dto.auth.EmployeeRegistrationResponse;
import com.tuon.tbank.dto.person.*;
import com.tuon.tbank.entity.person.Employee;
import com.tuon.tbank.enums.person.EmployeeRole;
import com.tuon.tbank.enums.person.EmployeeStatus;
import com.tuon.tbank.service.person.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * Exposes REST endpoints for employee registration, retrieval, profile updates,
 * password management, status and role management, and logical deletion.
 */
@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * Constructs an EmployeeController with the specified EmployeeService.
     *
     * @param employeeService the service for managing employees
     */
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Retrieves a list of all employees, including those logically deleted, and returns them as a list of {@link EmployeeProfileResponse} objects.
     *
     * @return a ResponseEntity containing a list of EmployeeProfileResponse objects
     */
    @GetMapping
    @PreAuthorize("@employeeAuthorization.canSearchEmployees(authentication)")
    public ResponseEntity<List<EmployeeProfileResponse>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        List<EmployeeProfileResponse> response = employees.stream()
                .map(employee -> new EmployeeProfileResponse
                        (employee.getId(), employee.getName(), employee.getEmail(),
                                employee.getRole(), employee.getStatus(), employee.getAdmissionDate()))
                .toList();
        return ResponseEntity.ok().body(response);
    }

    /**
     * Retrieves a list of all logically deleted employees and returns them as a list of {@link EmployeeProfileResponse} objects.
     *
     * @return a ResponseEntity containing a list of EmployeeProfileResponse objects for deleted employees
     */
    @GetMapping("/deleted")
    @PreAuthorize("@employeeAuthorization.canSearchEmployees(authentication)")
    public ResponseEntity<List<EmployeeProfileResponse>> getAllDeletedEmployees() {
        List<Employee> employee = employeeService.getAllDeletedEmployees();
        List<EmployeeProfileResponse> response = employee.stream()
                .map(emp -> new EmployeeProfileResponse
                        (emp.getId(), emp.getName(), emp.getEmail(),
                                emp.getRole(), emp.getStatus(), emp.getAdmissionDate()))
                .toList();
        return ResponseEntity.ok().body(response);
    }

    /**
     * Retrieves a list of all non-deleted employees and returns them as a list of {@link EmployeeProfileResponse} objects.
     *
     * @return a ResponseEntity containing a list of EmployeeProfileResponse objects for non-deleted employees
     */
    @GetMapping("/active")
    @PreAuthorize("@employeeAuthorization.canSearchEmployees(authentication)")
    public ResponseEntity<List<EmployeeProfileResponse>> getAllNonDeletedEmployees() {
        List<Employee> employee = employeeService.getAllNonDeletedEmployees();
        List<EmployeeProfileResponse> response = employee.stream()
                .map(emp -> new EmployeeProfileResponse
                        (emp.getId(), emp.getName(), emp.getEmail(),
                                emp.getRole(), emp.getStatus(), emp.getAdmissionDate()))
                .toList();
        return ResponseEntity.ok().body(response);
    }

    /**
     * Retrieves an employee by their ID and returns their profile information as an {@link EmployeeProfileResponse} object.
     *
     * @param id the ID of the employee to retrieve
     * @return a ResponseEntity containing the EmployeeProfileResponse object for the specified employee
     */
    @GetMapping("/{id}")
    @PreAuthorize("@employeeAuthorization.canSearchEmployees(authentication)")
    public ResponseEntity<EmployeeProfileResponse> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok().body(new EmployeeProfileResponse
                (employee.getId(), employee.getName(), employee.getEmail(),
                        employee.getRole(), employee.getStatus(), employee.getAdmissionDate()));
    }

    /**
     * Retrieves a list of employees whose birthdays fall within the specified range and returns them as a list of {@link EmployeeProfileResponse} objects.
     *
     * @param startDate the start date of the birthdate range
     * @param endDate   the end date of the birthdate range
     * @return a ResponseEntity containing a list of EmployeeProfileResponse objects for employees within the specified birthdate range
     */
    @GetMapping("/birthdates")
    @PreAuthorize("@employeeAuthorization.canSearchEmployees(authentication)")
    public ResponseEntity<List<EmployeeProfileResponse>> getEmployeesByBirthDateRange(LocalDate startDate, LocalDate endDate) {
        List<Employee> employees = employeeService.getEmployeesByBirthDateRange(startDate, endDate);
        List<EmployeeProfileResponse> response = employees.stream()
                .map(employee -> new EmployeeProfileResponse
                        (employee.getId(), employee.getName(), employee.getEmail(),
                                employee.getRole(), employee.getStatus(), employee.getAdmissionDate()))
                .toList();
        return ResponseEntity.ok().body(response);
    }

    /**
     * Retrieves an employee by their document number and returns their profile information as an {@link EmployeeProfileResponse} object.
     *
     * @param document the document number of the employee to retrieve
     * @return a ResponseEntity containing the EmployeeProfileResponse object for the specified employee
     */
    @GetMapping("/document")
    @PreAuthorize("@employeeAuthorization.canSearchEmployees(authentication)")
    public ResponseEntity<EmployeeProfileResponse> getEmployeeByDocument(String document) {
        Employee employee = employeeService.getEmployeeByDocument(document);
        return ResponseEntity.ok().body(new EmployeeProfileResponse
                (employee.getId(), employee.getName(), employee.getEmail(),
                        employee.getRole(), employee.getStatus(), employee.getAdmissionDate()));
    }

    /**
     * Retrieves an employee by their email address and returns their profile information as an {@link EmployeeProfileResponse} object.
     *
     * @param email the email address of the employee to retrieve
     * @return a ResponseEntity containing the EmployeeProfileResponse object for the specified employee
     */
    @GetMapping("/email")
    @PreAuthorize("@employeeAuthorization.canSearchEmployees(authentication)")
    public ResponseEntity<EmployeeProfileResponse> getEmployeeByEmail(String email) {
        Employee employee = employeeService.getEmployeeByEmail(email);
        return ResponseEntity.ok().body(new EmployeeProfileResponse
                (employee.getId(), employee.getName(), employee.getEmail(),
                        employee.getRole(), employee.getStatus(), employee.getAdmissionDate()));
    }

    /**
     * Retrieves an employee by their email address, including logically deleted employees, and returns their profile information as an {@link EmployeeProfileResponse} object.
     *
     * @param email the email address of the employee to retrieve
     * @return a ResponseEntity containing the EmployeeProfileResponse object for the specified employee
     */
    @GetMapping("/email/all")
    @PreAuthorize("@employeeAuthorization.canSearchEmployees(authentication)")
    public ResponseEntity<EmployeeProfileResponse> getEmployeeByEmailIncludingDeleted(String email) {
        Employee employee = employeeService.getEmployeeByEmailIncludingDeleted(email);
        return ResponseEntity.ok().body(new EmployeeProfileResponse
                (employee.getId(), employee.getName(), employee.getEmail(),
                        employee.getRole(), employee.getStatus(), employee.getAdmissionDate()));
    }

    /**
     * Retrieves a list of employees with the specified role and returns them as a list of {@link EmployeeProfileResponse} objects.
     *
     * @param role the role of the employees to retrieve
     * @return a ResponseEntity containing a list of EmployeeProfileResponse objects for employees with the specified role
     */
    @GetMapping("/role")
    @PreAuthorize("@employeeAuthorization.canSearchEmployees(authentication)")
    public ResponseEntity<List<EmployeeProfileResponse>> getEmployeesByRole(EmployeeRole role) {
        List<Employee> employees = employeeService.getEmployeesByRole(role);
        List<EmployeeProfileResponse> response = employees.stream()
                .map(employee -> new EmployeeProfileResponse
                        (employee.getId(), employee.getName(), employee.getEmail(),
                                employee.getRole(), employee.getStatus(), employee.getAdmissionDate()))
                .toList();
        return ResponseEntity.ok().body(response);
    }

    /**
     * Retrieves a list of employees whose admission dates fall within the specified range and returns them as a list of {@link EmployeeProfileResponse} objects.
     *
     * @param startDate the start date of the admission date range
     * @param endDate   the end date of the admission date range
     * @return a ResponseEntity containing a list of EmployeeProfileResponse objects for employees within the specified admission date range
     */
    @GetMapping("/admission-date")
    @PreAuthorize("@employeeAuthorization.canSearchEmployees(authentication)")
    public ResponseEntity<List<EmployeeProfileResponse>> getEmployeeByAdmissionDateRange(LocalDate startDate, LocalDate endDate) {
        List<Employee> employees = employeeService.getEmployeeByAdmissionDateRange(startDate, endDate);
        List<EmployeeProfileResponse> response = employees.stream()
                .map(employee -> new EmployeeProfileResponse
                        (employee.getId(), employee.getName(), employee.getEmail(),
                                employee.getRole(), employee.getStatus(), employee.getAdmissionDate()))
                .toList();
        return ResponseEntity.ok().body(response);
    }

    /**
     * Retrieves a list of employees with the specified status and returns them as a list of {@link EmployeeProfileResponse} objects.
     *
     * @param status the status of the employees to retrieve
     * @return a ResponseEntity containing a list of EmployeeProfileResponse objects for employees with the specified status
     */
    @GetMapping("/status")
    @PreAuthorize("@employeeAuthorization.canSearchEmployees(authentication)")
    public ResponseEntity<List<EmployeeProfileResponse>> getEmployeesByStatus(EmployeeStatus status) {
        List<Employee> employees = employeeService.getEmployeesByStatus(status);
        List<EmployeeProfileResponse> response = employees.stream()
                .map(employee -> new EmployeeProfileResponse
                        (employee.getId(), employee.getName(), employee.getEmail(),
                                employee.getRole(), employee.getStatus(), employee.getAdmissionDate()))
                .toList();
        return ResponseEntity.ok().body(response);
    }

    /**
     * Retrieves a list of employees with the specified role and status, and returns them as a list of {@link EmployeeProfileResponse} objects.
     *
     * @param role   the role of the employees to retrieve
     * @param status the status of the employees to retrieve
     * @return a ResponseEntity containing a list of EmployeeProfileResponse objects for employees with the specified role and status
     */
    @GetMapping("/role-status")
    @PreAuthorize("@employeeAuthorization.canSearchEmployees(authentication)")
    public ResponseEntity<List<EmployeeProfileResponse>> getEmployeesByRoleAndStatus(EmployeeRole role, EmployeeStatus status) {
        List<Employee> employees = employeeService.getEmployeesByRoleAndStatus(role, status);
        List<EmployeeProfileResponse> response = employees.stream()
                .map(employee -> new EmployeeProfileResponse
                        (employee.getId(), employee.getName(), employee.getEmail(),
                                employee.getRole(), employee.getStatus(), employee.getAdmissionDate()))
                .toList();
        return ResponseEntity.ok().body(response);
    }

    /**
     * Registers a new employee with the provided information and returns the created employee's details as an {@link EmployeeRegistrationResponse} object.
     *
     * @param request the request containing the employee registration information
     * @return a ResponseEntity containing the EmployeeRegistrationResponse object for the newly registered employee
     */
    @PostMapping
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public ResponseEntity<EmployeeRegistrationResponse> insertEmployee(
            @Valid @RequestBody EmployeeRegistrationRequest request) {
        Employee obj = employeeService.registerEmployee(request.name(), request.birthDate(),
                request.document(), request.email(), request.password(), request.role());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(obj.getId()).toUri();
        return ResponseEntity.created(uri).body(new EmployeeRegistrationResponse(obj.getId(),
                obj.getName(), obj.getEmail(), obj.getRole(),
                obj.getStatus(), obj.getAdmissionDate()));
    }

    /**
     * Updates the information of an existing employee with the specified ID using the provided update request.
     *
     * @param id      the ID of the employee to update
     * @param request the request containing the updated employee information
     * @return a ResponseEntity containing the EmployeeProfileResponse object for the updated employee
     */
    @PutMapping("/{id}")
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public ResponseEntity<EmployeeProfileResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request) {

        Employee employee = employeeService.getEmployeeById(id);
        employee.setName(request.name());
        employee.setBirthDate(request.birthDate());
        employee.setDocument(request.document());
        employee.setEmail(request.email());

        Employee obj = employeeService.updateEmployee(id, employee);

        return ResponseEntity.ok(new EmployeeProfileResponse(
                obj.getId(),
                obj.getName(),
                obj.getEmail(),
                obj.getRole(),
                obj.getStatus(),
                obj.getAdmissionDate()));
    }

    /**
     * Updates the salary of an existing employee with the specified ID using the provided salary request.
     *
     * @param id      the ID of the employee to update
     * @param request the request containing the updated salary information
     * @return a ResponseEntity containing the EmployeeProfileResponse object for the updated employee
     */
    @PatchMapping("/{id}/salary")
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public ResponseEntity<EmployeeProfileResponse> setEmployeeSalary(@PathVariable Long id, @Valid @RequestBody EmployeeSalaryRequest request) {
        Employee obj = employeeService.setEmployeeSalary(id, request.salary());
        return ResponseEntity.ok().body(new EmployeeProfileResponse(obj.getId(), obj.getName(), obj.getEmail(),
                obj.getRole(), obj.getStatus(), obj.getAdmissionDate()));
    }

    /**
     * Updates the role of an existing employee with the specified ID using the provided role request.
     *
     * @param id      the ID of the employee to update
     * @param request the request containing the updated role information
     * @return a ResponseEntity containing the EmployeeProfileResponse object for the updated employee
     */
    @PatchMapping("/{id}/role")
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public ResponseEntity<EmployeeProfileResponse> setEmployeeRole(@PathVariable Long id, @Valid @RequestBody EmployeeRoleRequest request) {
        Employee obj = employeeService.setEmployeeRole(id, request.role());
        return ResponseEntity.ok().body(new EmployeeProfileResponse(obj.getId(), obj.getName(), obj.getEmail(),
                obj.getRole(), obj.getStatus(), obj.getAdmissionDate()));
    }

    /**
     * Puts an existing employee with the specified ID on leave.
     *
     * @param id the ID of the employee to put on leave
     * @return a ResponseEntity containing the EmployeeProfileResponse object for the employee on leave
     */
    @PatchMapping("/{id}/leave")
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public ResponseEntity<EmployeeProfileResponse> putEmployeeOnLeave(@PathVariable Long id) {
        Employee obj = employeeService.putEmployeeOnLeave(id);
        return ResponseEntity.ok(new EmployeeProfileResponse(obj.getId(), obj.getName(), obj.getEmail(),
                obj.getRole(), obj.getStatus(), obj.getAdmissionDate()));
    }

    /**
     * Terminates an existing employee with the specified ID.
     *
     * @param id the ID of the employee to terminate
     * @return a ResponseEntity containing the EmployeeProfileResponse object for the terminated employee
     */
    @PatchMapping("/{id}/terminate")
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public ResponseEntity<EmployeeProfileResponse> terminateEmployee(@PathVariable Long id) {
        Employee obj = employeeService.terminateEmployee(id);
        return ResponseEntity.ok(new EmployeeProfileResponse(obj.getId(), obj.getName(), obj.getEmail(),
                obj.getRole(), obj.getStatus(), obj.getAdmissionDate()));
    }

    /**
     * Reactivates an existing employee with the specified ID.
     *
     * @param id the ID of the employee to reactivate
     * @return a ResponseEntity containing the EmployeeProfileResponse object for the reactivated employee
     */
    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public ResponseEntity<EmployeeProfileResponse> reactivateEmployee(@PathVariable Long id) {
        Employee obj = employeeService.reactivateEmployee(id);
        return ResponseEntity.ok(new EmployeeProfileResponse(obj.getId(), obj.getName(), obj.getEmail(),
                obj.getRole(), obj.getStatus(), obj.getAdmissionDate()));
    }

    /**
     * Deletes an existing employee with the specified ID.
     * Returns HTTP 204 No Content if the deletion is successful.
     *
     * @param id the ID of the employee to delete
     * @return a ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Changes the password of the employee with the specified ID.
     * Returns HTTP 204 No Content if the password change is successful.
     *
     * @param id      the ID of the employee whose password is to be changed
     * @param request the request containing the current and new passwords
     * @return a ResponseEntity with no content
     */
    @PatchMapping("/{id}/password")
    @PreAuthorize("@employeeAuthorization.isOwner(#id, authentication)")
    public ResponseEntity<Void> changeOwnPassword(@PathVariable Long id,
                                                  @Valid @RequestBody EmployeePasswordChangeRequest request) {
        employeeService.changeOwnPassword(id, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    /**
     * Resets the password of the employee with the specified ID.
     * Returns HTTP 204 No Content if the password reset is successful.
     *
     * @param id      the ID of the employee whose password is to be reset
     * @param request the request containing the new password
     * @return a ResponseEntity with no content
     */
    @PatchMapping("/{id}/password/reset")
    @PreAuthorize("@employeeAuthorization.canManageEmployees(authentication)")
    public ResponseEntity<Void> resetEmployeePassword(@PathVariable Long id,
                                                      @Valid @RequestBody EmployeePasswordResetRequest request) {
        employeeService.resetEmployeePassword(id, request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
