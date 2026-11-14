package com.tuon.tbank.dto.account;

import jakarta.validation.constraints.NotNull;

/**
 * Represents a request to update the status of an account.
 *
 * <p>This record is used to encapsulate the necessary information for updating the status of an account.</p>
 *
 * @param accountNumber The unique identifier of the account whose status is to be updated.
 */
public record AccountStatusUpdateRequest(@NotNull String accountNumber) {
}
