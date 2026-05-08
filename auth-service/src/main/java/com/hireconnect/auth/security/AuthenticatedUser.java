package com.hireconnect.auth.security;

import com.hireconnect.auth.entity.Role;

public record AuthenticatedUser(Long userId, String email, Role role) {
}
