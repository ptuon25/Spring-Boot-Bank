package com.tuon.tbank.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Clock;
import java.util.Base64;

/**
 * Configures HTTP security, password encoding, JWT handling, and authentication beans.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String encodedJwtSecret;

    @Value("${app.jwt.issuer}")
    private String jwtIssuer;

    @Value("${app.security.h2-console-enabled:false}")
    private boolean h2ConsoleEnabled;

    /**
     * Configures the stateless HTTP security filter chain, including request authorization
     * and JWT resource-server support.
     *
     * @param http the HTTP security configuration
     * @param jwtAuthenticationConverter the converter used to derive authorities from JWTs
     * @return the configured security filter chain
     * @throws Exception if the security filter chain cannot be built
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                  JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    if (h2ConsoleEnabled) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }
                    auth.requestMatchers(HttpMethod.POST, "/auth/login", "/clients").permitAll()
                            .requestMatchers(HttpMethod.GET, "/clients/deleted")
                            .hasAnyAuthority("ROLE_MANAGER" ,"ROLE_ADMIN")
                            .requestMatchers(HttpMethod.GET, "/clients/search/**", "/clients")
                            .hasAnyAuthority("ROLE_ASSOCIATE", "ROLE_MANAGER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.GET, "/clients/{id}")
                            .hasAnyAuthority("ROLE_CLIENT", "ROLE_MANAGER",  "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.PATCH, "/clients/{id}/password")
                            .hasAuthority("ROLE_CLIENT")
                            .requestMatchers("/clients/**").hasAnyAuthority("ROLE_MANAGER","ROLE_ADMIN")
                            .requestMatchers("/employees/**", "/audits/**").hasAuthority("ROLE_ADMIN")
                            .requestMatchers(HttpMethod.POST, "/business-accounts", "/business-accounts/**")
                            .hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")
                            .anyRequest().hasAnyAuthority("ROLE_ASSOCIATE", "ROLE_MANAGER", "ROLE_ADMIN");
                })
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(HttpStatus.FORBIDDEN.value())))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        if (h2ConsoleEnabled) {
            http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        }
        return http.build();
    }

    /**
     * Creates a JWT encoder using the configured signing key.
     *
     * @param jwtSigningKey the key used to sign JWTs
     * @return a JWT encoder
     */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSigningKey));
    }

    /**
     * Creates a JWT decoder that uses HS256 and validates the configured issuer.
     *
     * @param jwtSigningKey the key used to verify JWT signatures
     * @return a JWT decoder
     */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSigningKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSigningKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtIssuer));
        return decoder;
    }

    /**
     * Decodes and validates the configured Base64-encoded JWT signing key.
     *
     * @return the HMAC key used for JWT signing and verification
     * @throws IllegalStateException if the secret is missing, invalid Base64, or shorter than 32 bytes
     */
    @Bean
    public SecretKey jwtSigningKey() {
        if (encodedJwtSecret == null || encodedJwtSecret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be configured with a Base64-encoded key");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(encodedJwtSecret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET must be valid Base64", exception);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes for HS256");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /**
     * Creates the password encoder used by the application.
     *
     * @return a BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provides a UTC system clock.
     *
     * @return the UTC system clock
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * Obtains the authentication manager configured by Spring Security.
     *
     * @param configuration the authentication configuration
     * @return the configured authentication manager
     * @throws Exception if the authentication manager cannot be obtained
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Configures JWT authorities to be read from the {@code authorities} claim without a prefix.
     *
     * @return the JWT authentication converter
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("authorities");
        authoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}
