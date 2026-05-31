package com.driverkonnect.backend.dto.request.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticatedUserDto {
    private String username;
    private String role;
    private String ip;
}
