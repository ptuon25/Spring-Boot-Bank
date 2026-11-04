package com.tuon.tbank.dto.auth;

/**
 * Contains the public company details returned after registration.
 *
 * @param id the identifier assigned to the company
 * @param tradeName the company's trade name
 * @param partnerId the identifier of the associated partner
 * @param registrationNumber the company's registration number
 */
public record CompanyRegistrationResponse(Long id,
                                          String tradeName,
                                          Long partnerId,
                                          String registrationNumber) {
}
