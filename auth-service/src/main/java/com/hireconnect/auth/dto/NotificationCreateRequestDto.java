package com.hireconnect.auth.dto;

import com.hireconnect.auth.entity.NotificationType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationCreateRequestDto {

    private Long recipientUserId;
    private String recipientEmail;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean sendEmail;
}