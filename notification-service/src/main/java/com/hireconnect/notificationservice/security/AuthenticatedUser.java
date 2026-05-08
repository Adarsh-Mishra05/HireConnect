package com.hireconnect.notificationservice.security;

import com.hireconnect.notificationservice.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticatedUser {
    private Long userId;
    private String email;
    private Role role;
}