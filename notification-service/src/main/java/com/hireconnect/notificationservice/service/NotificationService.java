package com.hireconnect.notificationservice.service;

import org.springframework.data.domain.Page;

import com.hireconnect.notificationservice.dto.request.NotificationCreateRequestDto;
import com.hireconnect.notificationservice.dto.request.RecruiterMessageRequestDto;
import com.hireconnect.notificationservice.dto.response.NotificationResponseDto;
import com.hireconnect.notificationservice.security.AuthenticatedUser;

public interface NotificationService {

    NotificationResponseDto createNotification(NotificationCreateRequestDto requestDto);

    Page<NotificationResponseDto> getMyNotifications(AuthenticatedUser user, int page, int size);

    long getUnreadCount(AuthenticatedUser user);

    NotificationResponseDto markAsRead(AuthenticatedUser user, Long notificationId);

    long markAllAsRead(AuthenticatedUser user);

    void deleteNotification(AuthenticatedUser user, Long notificationId);

    NotificationResponseDto sendRecruiterMessageToCandidate(
            AuthenticatedUser user,
            RecruiterMessageRequestDto requestDto
    );
}
