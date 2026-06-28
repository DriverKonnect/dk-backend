package com.driverkonnect.backend.service;

import com.driverkonnect.backend.dto.request.account.PasswordUpdateRequestDto;

public interface AccountService {

    void updatePassword(PasswordUpdateRequestDto request);
}
