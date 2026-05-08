package com.hireconnect.applicationservice.security;

import com.hireconnect.applicationservice.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticatedUser {
    private Long userId;
    private String email;
    private Role role;
}