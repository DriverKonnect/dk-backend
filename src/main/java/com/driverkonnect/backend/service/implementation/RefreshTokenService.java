package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.entity.RefreshToken;
import com.driverkonnect.backend.entity.User;
import com.driverkonnect.backend.repository.RefreshTokenRepository;
import com.driverkonnect.backend.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${app.jwt.refresh.secret}")
    private String refreshSecret;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;


    @Transactional
    public RefreshToken createRefreshToken(String username) {
        log.debug("Creating refresh token for user: {}", username);
        User user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();
        log.debug("Existing refresh token removed for user: {}", username);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(generateRefreshToken(username));
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpiration));

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.debug("New refresh token created for user: {}", username);
        return saved;
    }

    @Transactional
    public void verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            log.warn("Refresh token expired for user: {}",
                    token.getUser() != null ? token.getUser().getEmail() : "unknown");
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired. Please login again.");
        }
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void deleteKeyByUsername(String username) {
        log.debug("Deleting refresh token for user: {}", username);
        User user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        refreshTokenRepository.deleteByUser(user);
        log.debug("Refresh token deleted for user: {}", username);
    }

    private String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getRefreshSigningKey())
                .compact();
    }

    private SecretKey getRefreshSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(refreshSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
