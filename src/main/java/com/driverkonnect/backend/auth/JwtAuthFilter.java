package com.driverkonnect.backend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtService.isTokenValid(token)) {
                String username = jwtService.extractUsername(token);
                String role = jwtService.extractRole(token);

                JwtAuthenticationToken auth = new JwtAuthenticationToken(username, role);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("JWT authentication set for user: {}", username);
            } else {
                log.warn("Invalid JWT token received for {} {}", request.getMethod(), request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, "accessToken");
        if (cookie != null && !cookie.getValue().isBlank()) {
            log.debug("JWT token extracted from accessToken cookie");
            return cookie.getValue();
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.debug("JWT token extracted from Authorization header");
            return authHeader.substring(7);
        }
        return null;
    }
}
