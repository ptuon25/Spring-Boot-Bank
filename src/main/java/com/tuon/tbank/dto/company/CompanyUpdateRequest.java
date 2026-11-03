package com.tuon.tbank.dto.company;

import com.tuon.tbank.entity.company.Company;
import com.tuon.tbank.entity.person.Person;

/**
 * Represents the company data provided to the API for a profile update.
 *
 * <p>Allows the API to receive only the information required
 * for updating a company's profile, without exposing or accepting
 * the entire {@link Company} entity.</p>
 *
 * @param legalName          the company's legal name
 * @param tradeName          the company's trade name
 * @param partner            the company's partner
 * @param registrationNumber the company's registration number
 */
public record CompanyUpdateRequest(
        String legalName,
        String tradeName,
        Person partner,
        String registrationNumber
) {
}
