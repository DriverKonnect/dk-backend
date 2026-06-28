package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.dto.request.account.PasswordUpdateRequestDto;
import com.driverkonnect.backend.entity.User;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.repository.UserRepository;
import com.driverkonnect.backend.service.AccountService;
import com.driverkonnect.backend.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public void updatePassword(PasswordUpdateRequestDto request) {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        log.debug("Processing password update for user: {}", email);

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new CustomException("New password and confirm password do not match", 400);
        }

        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new CustomException("User not found", 404));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password update failed — incorrect current password for user: {}", email);
            throw new CustomException("Current password is incorrect", 400);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new CustomException("New password must be different from the current password", 400);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        refreshTokenService.deleteKeyByUsername(user.getUsername());
        log.debug("Password updated and session invalidated for user: {}", email);
    }
}
