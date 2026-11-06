package com.tuon.tbank.dto.person;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

/**
 * Contains the employee data submitted for update.
 *
 * <p>This record is used to encapsulate the information required for updating an employee's details.</p>
 *
 * @param name the non-blank employee name
 * @param birthDate the non-null date of birth, which must not be in the future
 * @param document the non-blank identification document
 * @param email the non-blank email address, which must have a valid email format
 */
public record EmployeeUpdateRequest(
        @NotBlank String name,
        @NotNull @PastOrPresent LocalDate birthDate,
        @NotBlank String document,
        @NotBlank @Email String email) {
}
