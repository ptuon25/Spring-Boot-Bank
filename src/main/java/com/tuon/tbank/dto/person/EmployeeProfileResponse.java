package com.tuon.tbank.dto.person;

import com.tuon.tbank.entity.person.Employee;
import com.tuon.tbank.enums.person.EmployeeRole;
import com.tuon.tbank.enums.person.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Represents the employee data provided to the API for a profile response.
 *
 * <p>Allows the API to return only the information required
 * for displaying an employee's profile, without exposing or returning
 * the entire {@link Employee} entity.</p>
 *
 * @param id    the employee's unique identifier
 * @param name  the employee's full name
 * @param email the employee's e-mail address
 * @param role  the employee's role within the bank
 * @param status the employee's status within the bank
 * @param admissionDate the date the employee was admitted to the bank
 */
public record EmployeeProfileResponse(
        @NotNull @Positive Long id,
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotNull EmployeeRole role,
        @NotNull EmployeeStatus status,
        @NotNull LocalDate admissionDate) {
}
