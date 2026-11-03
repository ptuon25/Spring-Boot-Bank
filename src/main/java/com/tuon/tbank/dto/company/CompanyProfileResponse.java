package com.tuon.tbank.dto.company;

import com.tuon.tbank.entity.company.Company;
import com.tuon.tbank.entity.person.Person;

/**
 * Represents the basic company data returned by the API.
 *
 * <p>Allows the API to expose only necessary information about the company, avoiding
 * exposing unnecessary or sensitive information from the {@link Company} entity.</p>
 *
 * @param id                 the company's identifier in the database
 * @param tradeName          the company's trade name
 * @param partner            the company's partner
 * @param registrationNumber the company's registration number (CNPJ)
 */
public record CompanyProfileResponse(
        Long id,
        String tradeName,
        Person partner,
        String registrationNumber
) {
}
