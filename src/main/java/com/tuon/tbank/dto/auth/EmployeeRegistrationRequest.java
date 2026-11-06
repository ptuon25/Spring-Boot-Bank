package com.tuon.tbank.dto.auth;

import com.tuon.tbank.enums.person.EmployeeRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Contains the employee data submitted for registration.
 *
 * <p>The employee status and admission date are assigned by the service.
 * Salary is managed separately and is intentionally not part of registration.</p>
 */
public record EmployeeRegistrationRequest(
        @NotBlank String name,
        @NotNull @PastOrPresent LocalDate birthDate,
        @NotBlank String document,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 12, max = 72) String password,
        @NotNull EmployeeRole role
) {
}
