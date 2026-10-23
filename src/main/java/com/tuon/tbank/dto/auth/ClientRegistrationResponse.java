package com.tuon.tbank.dto.auth;

/**
 * Contains the public client details returned after registration.
 *
 * @param id the identifier assigned to the client
 * @param name the client's name
 * @param email the client's email address
 */
public record ClientRegistrationResponse(Long id, String name, String email) {
}
