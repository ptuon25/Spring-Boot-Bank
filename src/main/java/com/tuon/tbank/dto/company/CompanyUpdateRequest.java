package com.tuon.tbank.dto.company;

import com.tuon.tbank.entity.company.Company;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Represents the company data provided to the API for a profile update.
 *
 * <p>Allows the API to receive only the information required
 * for updating a company's profile, without exposing or accepting
 * the entire {@link Company} entity.</p>
 *
 * @param legalName          the company's legal name
 * @param tradeName          the company's trade name
 * @param partnerId          the company's partner identifier in the database
 * @param registrationNumber the company's registration number
 */
public record CompanyUpdateRequest(
        @NotBlank String legalName,
        @NotBlank String tradeName,
        @NotNull @Positive Long partnerId,
        @NotBlank String registrationNumber
) {
}
