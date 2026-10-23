package com.tuon.tbank.dto.person;

import com.tuon.tbank.entity.person.Client;

/**
 * Represents the basic client data returned by the API.
 *
 * <p>Allows the API to expose only necessary information about the client, avoiding
 * exposing unnecessary or sensitive information from the {@link Client} entity.</p>
 *
 * @param id       the client's identifier in the database
 * @param name     the client's full name
 * @param email    the client's e-mail address
 */
public record ClientProfileResponse(Long id, String name, String email) {
}
