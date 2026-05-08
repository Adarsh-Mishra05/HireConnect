package com.hireconnect.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.Data;

@FeignClient(name = "notification-service", path = "/internal/notifications")
public interface NotificationServiceClient {

    @PostMapping("/send-email")
    void sendEmail(@RequestBody SendEmailRequestDto requestDto);

    @Data
    class SendEmailRequestDto {
        private String to;
        private String subject;
        private String body;
    }
}
