package com.tuon.tbank.dto.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Represents a request to create a new savings account.
 * This record encapsulates the necessary information for creating a savings account.
 *
 * <p>This DTO is used to capture the details required for creating a new savings account in the system.</p>
 *
 * @param ownerId The ID of the owner of the savings account.
 */
public record SavingsAccountCreateRequest(@NotNull Long ownerId) {
}
