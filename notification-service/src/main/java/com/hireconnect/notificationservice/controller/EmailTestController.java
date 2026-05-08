package com.hireconnect.notificationservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hireconnect.notificationservice.service.EmailNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/internal/test")
@RequiredArgsConstructor
@Slf4j
public class EmailTestController {

    private final EmailNotificationService emailNotificationService;

    @PostMapping("/test-login-email")
    public ResponseEntity<String> testLoginEmail(@RequestParam String email, @RequestParam String userName) {
        try {
            emailNotificationService.sendLoginNotification(email, userName, "2024-04-29 16:30:00");
            return ResponseEntity.ok("Login test email sent successfully to " + email);
        } catch (Exception e) {
            log.error("Failed to send test login email", e);
            return ResponseEntity.internalServerError().body("Failed to send test email: " + e.getMessage());
        }
    }

    @PostMapping("/test-notification-email")
    public ResponseEntity<String> testNotificationEmail(
            @RequestParam String email,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam String type) {
        try {
            emailNotificationService.sendNotificationEmail(email, title, message, type);
            return ResponseEntity.ok("Notification test email sent successfully to " + email);
        } catch (Exception e) {
            log.error("Failed to send test notification email", e);
            return ResponseEntity.internalServerError().body("Failed to send test email: " + e.getMessage());
        }
    }
}
