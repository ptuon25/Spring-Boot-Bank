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
 * @param companyId The ID of the company for which the business account is being created. Must not be null.
 */
public record BusinessAccountCreateRequest(@NotNull Long companyId) {
}
