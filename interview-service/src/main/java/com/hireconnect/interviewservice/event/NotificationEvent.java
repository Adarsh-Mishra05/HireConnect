package com.hireconnect.interviewservice.event;

import com.hireconnect.interviewservice.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    private Long recipientUserId;
    private String recipientEmail;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean sendEmail;
}