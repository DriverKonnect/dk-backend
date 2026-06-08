package com.driverkonnect.backend.service;

import com.driverkonnect.backend.dto.request.auth.LoginRequestDto;
import com.driverkonnect.backend.dto.response.auth.TokenPairDto;

public interface AuthService {
    TokenPairDto login(LoginRequestDto request);
    TokenPairDto refreshAccessToken(String refreshToken);
    void logout(String refreshToken);
}
