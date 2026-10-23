package com.tuon.tbank.dto.auth;

/**
 * Contains an authentication error message returned by the API.
 *
 * @param error the error message
 */
public record AuthErrorResponse(String error) {
}
