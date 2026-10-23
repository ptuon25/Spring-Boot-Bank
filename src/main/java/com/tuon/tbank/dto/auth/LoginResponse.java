package com.tuon.tbank.dto.auth;

/**
 * Contains the access token and its bearer-token metadata returned after authentication.
 *
 * @param accessToken the encoded access token
 * @param tokenType the token type
 * @param expiresIn the token lifetime in seconds
 */
public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
