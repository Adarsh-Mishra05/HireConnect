package com.hireconnect.interviewservice.dto.request;

import com.hireconnect.interviewservice.enums.NotificationType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificationCreateRequestDto {

    private Long recipientUserId;
    private String recipientEmail;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean sendEmail;
}