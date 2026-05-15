package com.hireconnect.notificationservice.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hireconnect.notificationservice.dto.request.NotificationCreateRequestDto;
import com.hireconnect.notificationservice.dto.request.RecruiterMessageRequestDto;
import com.hireconnect.notificationservice.dto.response.NotificationResponseDto;
import com.hireconnect.notificationservice.entity.Notification;
import com.hireconnect.notificationservice.enums.Role;
import com.hireconnect.notificationservice.client.ApplicationServiceClient;
import com.hireconnect.notificationservice.exception.ResourceNotFoundException;
import com.hireconnect.notificationservice.exception.UnauthorizedException;
import com.hireconnect.notificationservice.mapper.NotificationMapper;
import com.hireconnect.notificationservice.repository.NotificationRepository;
import com.hireconnect.notificationservice.security.AuthenticatedUser;
import com.hireconnect.notificationservice.service.EmailService;
import com.hireconnect.notificationservice.service.EmailNotificationService;
import com.hireconnect.notificationservice.service.NotificationService;

import lombok.RequiredArgsConstructor;

// Handles notification creation from RabbitMQ events and user management (read/delete).
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;
    private final EmailNotificationService emailNotificationService;
    private final ApplicationServiceClient applicationServiceClient;

    @Override
    @Transactional
    public NotificationResponseDto createNotification(NotificationCreateRequestDto requestDto) {
        logger.info(
                "Creating notification for recipientUserId={}, type={}, sendEmail={}",
                requestDto.getRecipientUserId(),
                requestDto.getType(),
                requestDto.getSendEmail()
        );

        Notification notification = Notification.builder()
                .userId(requestDto.getRecipientUserId())
                .title(requestDto.getTitle())
                .message(requestDto.getMessage())
                .type(requestDto.getType())
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        logger.info("Notification persisted successfully with id={} for userId={}", savedNotification.getId(), savedNotification.getUserId());

        // Always send email if recipient email is provided
        if (requestDto.getRecipientEmail() != null && !requestDto.getRecipientEmail().isBlank()) {
            logger.info("Email dispatch enabled for notificationId={} to recipientEmail={}", savedNotification.getId(), requestDto.getRecipientEmail());
            
            try {
                emailNotificationService.sendNotificationEmail(
                        requestDto.getRecipientEmail(),
                        requestDto.getTitle(),
                        requestDto.getMessage(),
                        requestDto.getType().toString()
                );
            } catch (Exception e) {
                logger.error("Failed to send notification email to: {} for notificationId: {}", 
                        requestDto.getRecipientEmail(), savedNotification.getId(), e);
                // Fallback to basic email service
                emailService.sendEmail(
                        requestDto.getRecipientEmail(),
                        requestDto.getTitle(),
                        requestDto.getMessage()
                );
            }
        } else {
            logger.debug("Email dispatch skipped for recipientUserId={}", requestDto.getRecipientUserId());
        }

        return notificationMapper.toResponseDto(savedNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getMyNotifications(AuthenticatedUser user, int page, int size) {
        logger.info("Fetching notifications from database for userId={}, page={}, size={}", user.getUserId(), page, size);
        Pageable pageable = PageRequest.of(page, size);

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId(), pageable)
                .map(notificationMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(AuthenticatedUser user) {
        logger.info("Counting unread notifications for userId={}", user.getUserId());
        return notificationRepository.countByUserIdAndIsReadFalse(user.getUserId());
    }

    @Override
    @Transactional
    public NotificationResponseDto markAsRead(AuthenticatedUser user, Long notificationId) {
        logger.info("Marking notificationId={} as read for userId={}", notificationId, user.getUserId());

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUserId().equals(user.getUserId())) {
            logger.warn("User {} attempted to mark notification {} belonging to user {} as read", user.getUserId(), notificationId, notification.getUserId());
            throw new UnauthorizedException("You can mark only your own notifications as read");
        }

        notification.setIsRead(true);
        Notification updatedNotification = notificationRepository.save(notification);
        logger.info("Notification marked as read successfully. notificationId={}, userId={}", notificationId, user.getUserId());

        return notificationMapper.toResponseDto(updatedNotification);
    }

    @Override
    @Transactional
    public long markAllAsRead(AuthenticatedUser user) {
        logger.info("Marking all unread notifications as read for userId={}", user.getUserId());
        int updatedCount = notificationRepository.markAllAsReadByUserId(user.getUserId());
        logger.info("Marked {} notifications as read for userId={}", updatedCount, user.getUserId());
        return updatedCount;
    }

    @Override
    @Transactional
    public void deleteNotification(AuthenticatedUser user, Long notificationId) {
        logger.info("Deleting notificationId={} for userId={}", notificationId, user.getUserId());

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUserId().equals(user.getUserId())) {
            logger.warn("User {} attempted to delete notification {} belonging to user {}", user.getUserId(), notificationId, notification.getUserId());
            throw new UnauthorizedException("You can delete only your own notifications");
        }

        notificationRepository.delete(notification);
        logger.info("Notification deleted successfully. notificationId={}, userId={}", notificationId, user.getUserId());
    }

    @Override
    @Transactional
    public NotificationResponseDto sendRecruiterMessageToCandidate(
            AuthenticatedUser user,
            RecruiterMessageRequestDto requestDto
    ) {
        if (user.getRole() != Role.RECRUITER) {
            throw new UnauthorizedException("Only recruiters can send messages");
        }

        Boolean shortlisted = applicationServiceClient.isCandidateShortlistedForRecruiterJob(
                user.getUserId(),
                requestDto.getCandidateId(),
                requestDto.getJobId()
        );

        if (Boolean.FALSE.equals(shortlisted)) {
            throw new UnauthorizedException("You can only message shortlisted candidates for your jobs");
        }

        Notification notification = Notification.builder()
                .userId(requestDto.getCandidateId())
                .title("Message from Recruiter")
                .message("Job #" + requestDto.getJobId() + ": " + requestDto.getMessage())
                .type(com.hireconnect.notificationservice.enums.NotificationType.SYSTEM)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        logger.info("Recruiter message notification created. recruiterId={}, candidateId={}, notificationId={}",
                user.getUserId(), requestDto.getCandidateId(), saved.getId());

        return notificationMapper.toResponseDto(saved);
    }
}
