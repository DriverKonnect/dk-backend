package com.driverkonnect.backend.auth;

import com.driverkonnect.backend.entity.User;
import com.driverkonnect.backend.repository.UserRepository;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class JwtUserDetails implements UserDetailsService {

    private final UserRepository userRepository;

    public JwtUserDetails(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(@Nonnull String email)
            throws UsernameNotFoundException {

        log.debug("Loading user details for email: {}", email);

        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> {
                    log.warn("Authentication failed - user not found or inactive for email: {}", email);
                    return new BadCredentialsException("User not found with email: " + email);
                });

        log.debug("User details loaded successfully for email: {}", email);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getIsActive(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().getRole()))
        );
    }
}
