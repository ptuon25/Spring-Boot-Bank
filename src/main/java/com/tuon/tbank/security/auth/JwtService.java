package com.tuon.tbank.security.auth;

import com.tuon.tbank.dto.auth.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Creates signed JWT access tokens for authenticated bank users.
 */
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final Duration expiration;
    private final String issuer;
    private final Clock clock;

    /**
     * Creates a service that issues signed access tokens.
     *
     * @param jwtEncoder the encoder used to sign tokens
     * @param expiration the token lifetime, which must be positive
     * @param issuer the issuer claim included in each token
     * @param clock the clock used to determine token issue and expiration times
     * @throws IllegalArgumentException if the token lifetime is zero or negative
     */
    public JwtService(JwtEncoder jwtEncoder,
                     @Value("${app.jwt.expiration}") Duration expiration,
                     @Value("${app.jwt.issuer}") String issuer,
                     Clock clock) {
        if (expiration.isZero() || expiration.isNegative()) {
            throw new IllegalArgumentException("JWT expiration must be positive");
        }
        this.jwtEncoder = jwtEncoder;
        this.expiration = expiration;
        this.issuer = issuer;
        this.clock = clock;
    }

    /**
     * Issues a bearer token for the supplied user, including its identity and authorities.
     *
     * @param user the authenticated user's details
     * @return the access token response with its token type and lifetime in seconds
     */
    public LoginResponse issueToken(BankUserDetails user) {
        Instant issuedAt = clock.instant();
        List<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(expiration))
                .subject(user.getIdentityType().name().toLowerCase() + ":" + user.getId())
                .claim("user_id", user.getId())
                .claim("identity_type", user.getIdentityType().name())
                .claim("authorities", authorities)
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", expiration.toSeconds());
    }
}
