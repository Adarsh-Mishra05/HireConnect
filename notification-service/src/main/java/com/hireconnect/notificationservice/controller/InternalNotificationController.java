package com.hireconnect.notificationservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hireconnect.notificationservice.dto.request.NotificationCreateRequestDto;
import com.hireconnect.notificationservice.dto.request.SendEmailRequestDto;
import com.hireconnect.notificationservice.dto.request.LoginNotificationRequestDto;
import com.hireconnect.notificationservice.dto.response.NotificationResponseDto;
import com.hireconnect.notificationservice.service.NotificationService;
import com.hireconnect.notificationservice.service.EmailService;
import com.hireconnect.notificationservice.service.EmailNotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private static final Logger logger = LoggerFactory.getLogger(InternalNotificationController.class);

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final EmailNotificationService emailNotificationService;

    @PostMapping
    public ResponseEntity<NotificationResponseDto> createNotification(
            @Valid @RequestBody NotificationCreateRequestDto requestDto
    ) {
        logger.info(
                "Internal create notification request received for recipientUserId={}, type={}, sendEmail={}",
                requestDto.getRecipientUserId(),
                requestDto.getType(),
                requestDto.getSendEmail()
        );

        NotificationResponseDto response = notificationService.createNotification(requestDto);

        logger.info("Notification created successfully with id={} for userId={}", response.getId(), response.getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/send-email")
    public ResponseEntity<Void> sendEmail(@Valid @RequestBody SendEmailRequestDto requestDto) {
        logger.info("Internal send email request received for to={}", requestDto.getTo());
        emailService.sendEmail(requestDto.getTo(), requestDto.getSubject(), requestDto.getBody());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/send-login-notification")
    public ResponseEntity<Void> sendLoginNotification(@RequestBody LoginNotificationRequestDto requestDto) {
        logger.info("Internal login notification request received for to={}, userName={}", 
                requestDto.getTo(), requestDto.getUserName());
        emailNotificationService.sendLoginNotification(
                requestDto.getTo(), 
                requestDto.getUserName(), 
                requestDto.getLoginTime()
        );
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
