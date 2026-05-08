package com.hireconnect.analyticsservice.security;

import com.hireconnect.analyticsservice.enums.Role;

public record AuthenticatedUser(Long userId, String email, Role role) {
}
