package com.tuon.tbank.dto.account;

import com.tuon.tbank.entity.company.Company;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating a business account.
 * <p>This record is used to encapsulate the necessary information for creating a new business account in the system.</p>
 *
 * @param balance The initial balance of the business account.
 * @param openingDate The date when the business account was opened.
 * @param owner The company that owns the business account.
 */
public record BusinessAccountCreateRequest(@NotNull BigDecimal balance,
                                           @NotNull @PastOrPresent LocalDate openingDate,
                                           @NotNull @Positive Company owner) {
}
