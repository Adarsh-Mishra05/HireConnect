package com.hireconnect.notificationservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hireconnect.notificationservice.dto.request.RecruiterMessageRequestDto;
import com.hireconnect.notificationservice.dto.response.NotificationResponseDto;
import com.hireconnect.notificationservice.security.AuthenticatedUser;
import com.hireconnect.notificationservice.service.NotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponseDto>> getMyNotifications(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        logger.info("Fetching notifications for userId={}, page={}, size={}", user.getUserId(), page, size);
        return ResponseEntity.ok(notificationService.getMyNotifications(user, page, size));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        logger.info("Fetching unread notification count for userId={}", user.getUserId());
        return ResponseEntity.ok(notificationService.getUnreadCount(user));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponseDto> markAsRead(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long notificationId
    ) {
        logger.info("Mark as read requested for notificationId={} by userId={}", notificationId, user.getUserId());
        return ResponseEntity.ok(notificationService.markAsRead(user, notificationId));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Long> markAllAsRead(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        logger.info("Mark all as read requested by userId={}", user.getUserId());
        return ResponseEntity.ok(notificationService.markAllAsRead(user));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long notificationId
    ) {
        logger.info("Delete notification requested for notificationId={} by userId={}", notificationId, user.getUserId());
        notificationService.deleteNotification(user, notificationId);
        logger.info("Notification deleted successfully. notificationId={}, userId={}", notificationId, user.getUserId());
        return ResponseEntity.ok("Notification deleted successfully");
    }

    @PostMapping("/messages/recruiter")
    public ResponseEntity<NotificationResponseDto> sendRecruiterMessage(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody RecruiterMessageRequestDto requestDto
    ) {
        logger.info("Recruiter message request for recruiterId={}, candidateId={}, jobId={}",
                user.getUserId(), requestDto.getCandidateId(), requestDto.getJobId());
        return ResponseEntity.ok(notificationService.sendRecruiterMessageToCandidate(user, requestDto));
    }
}
