package com.hireconnect.subscriptionservice.security;

import com.hireconnect.subscriptionservice.enums.Role;

public record AuthenticatedUser(Long userId, String email, Role role) {
}
