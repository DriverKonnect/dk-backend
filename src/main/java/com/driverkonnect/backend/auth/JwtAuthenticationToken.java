package com.driverkonnect.backend.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String username;
    private final String role;

    public JwtAuthenticationToken(String username, String role) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        this.username = username;
        this.role = role;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
