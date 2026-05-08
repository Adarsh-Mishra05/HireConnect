package com.hireconnect.paymentservice.security;

import com.hireconnect.paymentservice.enums.Role;

public record AuthenticatedUser(Long userId, String email, Role role) {
}