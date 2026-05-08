package com.hireconnect.notificationservice.mapper;

import org.springframework.stereotype.Component;

import com.hireconnect.notificationservice.dto.response.NotificationResponseDto;
import com.hireconnect.notificationservice.entity.Notification;

@Component
public class NotificationMapper {

    public NotificationResponseDto toResponseDto(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}