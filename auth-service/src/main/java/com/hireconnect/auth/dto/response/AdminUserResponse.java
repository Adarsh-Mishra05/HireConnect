package com.hireconnect.auth.dto.response;

import java.time.LocalDateTime;

import com.hireconnect.auth.entity.AuthProvider;
import com.hireconnect.auth.entity.Role;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserResponse {
    private Long userId;
    private String email;
    private String fullName;
    private Role role;
    private AuthProvider provider;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
