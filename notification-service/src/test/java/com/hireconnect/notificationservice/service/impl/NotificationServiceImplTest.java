package com.hireconnect.notificationservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.hireconnect.notificationservice.dto.request.NotificationCreateRequestDto;
import com.hireconnect.notificationservice.dto.response.NotificationResponseDto;
import com.hireconnect.notificationservice.entity.Notification;
import com.hireconnect.notificationservice.client.ApplicationServiceClient;
import com.hireconnect.notificationservice.enums.NotificationType;
import com.hireconnect.notificationservice.enums.Role;
import com.hireconnect.notificationservice.exception.ResourceNotFoundException;
import com.hireconnect.notificationservice.exception.UnauthorizedException;
import com.hireconnect.notificationservice.mapper.NotificationMapper;
import com.hireconnect.notificationservice.repository.NotificationRepository;
import com.hireconnect.notificationservice.security.AuthenticatedUser;
import com.hireconnect.notificationservice.service.EmailService;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private ApplicationServiceClient applicationServiceClient;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private AuthenticatedUser user;
    private NotificationCreateRequestDto requestDto;
    private Notification notification;
    private NotificationResponseDto responseDto;

    @BeforeEach
    void setUp() {
        user = new AuthenticatedUser(1L, "user@example.com", Role.CANDIDATE);

        requestDto = new NotificationCreateRequestDto();
        requestDto.setRecipientUserId(1L);
        requestDto.setRecipientEmail("user@example.com");
        requestDto.setTitle("Test Title");
        requestDto.setMessage("Test Message");
        requestDto.setType(NotificationType.APPLICATION);
        requestDto.setSendEmail(true);

        notification = Notification.builder()
                .id(10L)
                .userId(1L)
                .title("Test Title")
                .message("Test Message")
                .type(NotificationType.APPLICATION)
                .isRead(false)
                .build();

        responseDto = NotificationResponseDto.builder()
                .id(10L)
                .title("Test Title")
                .build();
    }

    @Test
    void createNotification_WithEmail_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());
        when(notificationMapper.toResponseDto(notification)).thenReturn(responseDto);

        NotificationResponseDto response = notificationService.createNotification(requestDto);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        verify(emailService, times(1)).sendEmail("user@example.com", "Test Title", "Test Message");
    }

    @Test
    void createNotification_WithoutEmail_Success() {
        requestDto.setSendEmail(false);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponseDto(notification)).thenReturn(responseDto);

        NotificationResponseDto response = notificationService.createNotification(requestDto);

        assertNotNull(response);
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponseDto(notification)).thenReturn(responseDto);

        NotificationResponseDto response = notificationService.markAsRead(user, 10L);

        assertNotNull(response);
        assertTrue(notification.getIsRead());
    }

    @Test
    void markAsRead_Unauthorized_ThrowsException() {
        notification.setUserId(2L); // Different user
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        assertThrows(UnauthorizedException.class, () -> 
            notificationService.markAsRead(user, 10L));
    }

    @Test
    void markAsRead_NotFound_ThrowsException() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            notificationService.markAsRead(user, 10L));
    }

    @Test
    void deleteNotification_Success() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        doNothing().when(notificationRepository).delete(notification);

        assertDoesNotThrow(() -> notificationService.deleteNotification(user, 10L));
        verify(notificationRepository, times(1)).delete(notification);
    }

    @Test
    void markAllAsRead_Success() {
        when(notificationRepository.markAllAsReadByUserId(1L)).thenReturn(3);

        long updated = notificationService.markAllAsRead(user);

        assertEquals(3L, updated);
        verify(notificationRepository, times(1)).markAllAsReadByUserId(1L);
    }
}
