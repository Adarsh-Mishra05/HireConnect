package com.hireconnect.profileservice.security;

import com.hireconnect.profileservice.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticatedUser {

    private Long userId;
    private Role role;
}