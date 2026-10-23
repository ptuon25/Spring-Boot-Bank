package com.tuon.tbank.dto.person;

import com.tuon.tbank.entity.person.Client;

import java.time.LocalDate;

/**
 * Represents the client data provided to the API for a profile update.
 *
 * <p>Allows the API to receive only the information required
 * for updating a client's profile, without exposing or accepting
 * the entire {@link Client} entity.</p>
 *
 * @param name        the client's full name
 * @param birthDate   the client's date of birth
 * @param document    the client's identification document
 * @param email       the client's e-mail address
 */
public record ClientUpdateRequest(String name, LocalDate birthDate, String document, String email) {
}
