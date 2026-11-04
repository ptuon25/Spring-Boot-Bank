package com.tuon.tbank.dto.auth;

import com.tuon.tbank.entity.person.Person;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Contains the company data submitted for registration.
 *
 * @param legalName the non-blank legal name of the company
 * @param tradeName the non-blank trade name of the company
 * @param registrationNumber the non-blank registration number of the company
 * @param partnerId the non-blank identifier of the principal partner of the company
 * @param foundationDate the non-null foundation date, which must not be in the future
 */
public record CompanyRegistrationRequest(
        @NotBlank String legalName,
        @NotBlank String tradeName,
        @NotBlank String registrationNumber,
        @NotNull @Positive Long partnerId,
        @NotNull @PastOrPresent LocalDate foundationDate
) { }
