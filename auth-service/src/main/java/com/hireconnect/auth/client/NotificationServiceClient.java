package com.hireconnect.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.Data;

@FeignClient(name = "notification-service", path = "/internal/notifications")
public interface NotificationServiceClient {

    @PostMapping("/send-email")
    void sendEmail(@RequestBody SendEmailRequestDto requestDto);

    @PostMapping("/send-login-notification")
    void sendLoginNotification(@RequestBody LoginNotificationRequestDto requestDto);

    @Data
    class SendEmailRequestDto {
        private String to;
        private String subject;
        private String body;
    }

    @Data
    class LoginNotificationRequestDto {
        private String to;
        private String userName;
        private String loginTime;
    }
}
