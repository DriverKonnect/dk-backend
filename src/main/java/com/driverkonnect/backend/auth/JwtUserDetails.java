package com.driverkonnect.backend.auth;

import com.driverkonnect.backend.entity.User;
import com.driverkonnect.backend.repository.UserRepository;
import jakarta.annotation.Nonnull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JwtUserDetails implements UserDetailsService {

    private final UserRepository userRepository;

    public JwtUserDetails(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(@Nonnull String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new BadCredentialsException(
                        "User not found with email: " + email));

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
