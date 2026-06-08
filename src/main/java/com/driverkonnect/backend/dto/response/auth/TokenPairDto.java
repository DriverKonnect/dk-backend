package com.driverkonnect.backend.dto.response.auth;

import com.driverkonnect.backend.dto.response.user.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenPairDto {
    private String accessToken;
    private String refreshToken;
    private UserResponseDto user;
}
