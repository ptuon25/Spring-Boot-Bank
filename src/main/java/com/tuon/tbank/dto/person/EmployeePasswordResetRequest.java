package com.tuon.tbank.dto.person;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents the request body used by an employee to reset their password.
 *
 * <p>The new password follows the same length rules as the registration request.</p>
 *
 * <p>The {@code @Size} constraint counts characters, while the service also
 * enforces BCrypt's 72-byte limit on the UTF-8 input.</p>
 *
 * @param newPassword the new plain-text password, between 12 and 72
 *                    characters
 */
public record EmployeePasswordResetRequest(@NotBlank @Size(min = 12, max = 72) String newPassword) {
}
