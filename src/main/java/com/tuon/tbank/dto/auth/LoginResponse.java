package com.tuon.tbank.dto.auth;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
