package com.tuon.tbank.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Contains the credentials submitted to authenticate a user.
 *
 * @param email the non-blank email address, which must have a valid email format
 * @param password the non-blank password
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
