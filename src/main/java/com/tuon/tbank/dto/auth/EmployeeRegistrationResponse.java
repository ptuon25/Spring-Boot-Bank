package com.tuon.tbank.dto.auth;

import com.tuon.tbank.enums.person.EmployeeRole;
import com.tuon.tbank.enums.person.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * DTO for employee registration response.
 *
 * <p>This record is used to represent the data returned when an employee is registered.</p>
 *
 * @param id            the employee's unique identifier
 * @param name          the employee's full name
 * @param email         the employee's e-mail address
 * @param role          the employee's role within the bank
 * @param status        the employee's status within the bank
 * @param admissionDate the date the employee was admitted to the bank
 */
public record EmployeeRegistrationResponse(@NotNull @Positive Long id,
                                           @NotBlank String name,
                                           @NotBlank @Email String email,
                                           @NotNull EmployeeRole role,
                                           @NotNull EmployeeStatus status,
                                           @NotNull LocalDate admissionDate) {
}
