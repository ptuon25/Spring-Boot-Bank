package com.tuon.tbank.dto.company;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

/**
 * Represents the foundation date submitted for a company update.
 *
 * @param foundationDate the non-null foundation date, which must not be in the future
 */
public record CompanyFoundationDateUpdateRequest(
        @NotNull @PastOrPresent LocalDate foundationDate
) {
}
