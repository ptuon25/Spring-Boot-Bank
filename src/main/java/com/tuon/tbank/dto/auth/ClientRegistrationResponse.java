package com.tuon.tbank.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Contains the public client details returned after registration.
 *
 * @param id the identifier assigned to the client
 * @param name the client's name
 * @param email the client's email address
 */
public record ClientRegistrationResponse(@Positive Long id,
                                         @NotBlank String name,
                                         @NotBlank @Email String email) {
}
