package com.hireconnect.auth.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.auth.dto.response.AdminUserResponse;
import com.hireconnect.auth.security.AuthenticatedUser;
import com.hireconnect.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminAuthController {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthController.class);

    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> getAllUsersForAdmin(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Admin users list request received by userId={}", user != null ? user.userId() : null);
        return ResponseEntity.ok(authService.getAllUsersForAdmin(user));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<AdminUserResponse> updateUserActiveStatus(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long userId,
            @RequestParam boolean active
    ) {
        log.info("Admin user status update request received by userId={} for targetUserId={} active={}",
                user != null ? user.userId() : null, userId, active);
        return ResponseEntity.ok(authService.updateUserActiveStatus(user, userId, active));
    }
}
