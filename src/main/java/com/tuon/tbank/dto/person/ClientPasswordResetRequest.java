package com.tuon.tbank.dto.person;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents the request body used by an employee to reset a client's password.
 *
 * <p>Unlike {@link ClientPasswordChangeRequest}, it does not require the
 * current password, since the operation is performed by an employee with the
 * {@code MANAGER} or {@code ADMIN} role.</p>
 *
 * @param newPassword the new plain-text password, between 12 and 72
 *                    characters
 */
public record ClientPasswordResetRequest(
        @NotBlank @Size(min = 12, max = 72) String newPassword) {
}
