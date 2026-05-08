package com.hireconnect.notificationservice.dto.response;

import java.time.LocalDateTime;

import com.hireconnect.notificationservice.enums.NotificationType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificationResponseDto {

    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private LocalDateTime createdAt;
}