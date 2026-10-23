package com.tuon.tbank.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Contains the client data submitted for registration.
 *
 * @param name the non-blank client name
 * @param birthDate the non-null date of birth, which must not be in the future
 * @param document the non-blank identification document
 * @param email the non-blank email address, which must have a valid email format
 * @param password the non-blank password, between 12 and 72 characters
 */
public record ClientRegistrationRequest(
        @NotBlank String name,
        @NotNull @PastOrPresent LocalDate birthDate,
        @NotBlank String document,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 12, max = 72) String password
) {
}
