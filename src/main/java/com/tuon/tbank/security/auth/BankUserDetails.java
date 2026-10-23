package com.tuon.tbank.security.auth;

import com.tuon.tbank.enums.auth.UserIdentityType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Supplies Spring Security with an account's identity, credentials, authority, and enabled state.
 */
public final class BankUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final UserIdentityType identityType;
    private final String authority;
    private final boolean enabled;

    /**
     * Creates user details for a client or employee account.
     *
     * @param id the account identifier
     * @param email the account email address used as the username
     * @param passwordHash the stored password hash
     * @param identityType the account identity type
     * @param authority the granted authority
     * @param enabled whether the account is enabled
     */
    public BankUserDetails(Long id, String email, String passwordHash, UserIdentityType identityType,
                           String authority, boolean enabled) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.identityType = identityType;
        this.authority = authority;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public UserIdentityType getIdentityType() {
        return identityType;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
