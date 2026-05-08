package com.hireconnect.notificationservice.dto.request;

import com.hireconnect.notificationservice.enums.NotificationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationCreateRequestDto {

    @NotNull(message = "Recipient userId is required")
    private Long recipientUserId;

    private String recipientEmail;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotNull(message = "sendEmail flag is required")
    private Boolean sendEmail;
}