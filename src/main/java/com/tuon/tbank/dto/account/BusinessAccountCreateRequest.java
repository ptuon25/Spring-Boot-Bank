package com.tuon.tbank.dto.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO for creating a business account.
 * <p>This record is used to encapsulate the necessary information for creating a new business account in the system.</p>
 *
 * @param ownerId The ID of the owner of the business account.
 */
public record BusinessAccountCreateRequest(@NotNull Long ownerId) {
}
