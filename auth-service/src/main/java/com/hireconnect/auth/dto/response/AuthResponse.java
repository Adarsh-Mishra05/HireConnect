package com.hireconnect.auth.dto.response;

import com.hireconnect.auth.entity.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private Long userId;
    private String email;
    private String fullName;
    private Role role;
    private String accessToken;
    private String refreshToken;
    private String message;
}
