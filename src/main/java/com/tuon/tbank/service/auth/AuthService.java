package com.tuon.tbank.service.auth;

import com.tuon.tbank.dto.auth.LoginRequest;
import com.tuon.tbank.dto.auth.LoginResponse;
import com.tuon.tbank.security.auth.BankUserDetails;
import com.tuon.tbank.security.auth.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email().trim(), request.password()));
        return jwtService.issueToken((BankUserDetails) authentication.getPrincipal());
    }
}
