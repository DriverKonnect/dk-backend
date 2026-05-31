package com.driverkonnect.backend.dto.response.auth;

import com.driverkonnect.backend.dto.response.user.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {
    private UserResponseDto user;
}
