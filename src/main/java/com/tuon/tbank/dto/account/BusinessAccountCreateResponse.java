package com.tuon.tbank.dto.account;

import com.tuon.tbank.entity.company.Company;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Represents the response for creating a business account.
 *
 * <p>This record encapsulates the details of a newly created business account, including its ID, account number,
 * opening date, and the associated company owner.</p>
 *
 * @param id            the unique identifier of the business account
 * @param accountNumber the account number of the business account
 * @param openingDate   the date when the business account was opened
 * @param companyOwner  the company that owns the business account
 */
public record BusinessAccountCreateResponse(
        @Positive Long id,
        @NotBlank String accountNumber,
        @NotNull @PastOrPresent LocalDate openingDate,
        @NotBlank @NotNull Company companyOwner
        ) {
}
