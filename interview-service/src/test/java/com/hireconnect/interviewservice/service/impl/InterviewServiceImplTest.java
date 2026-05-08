package com.hireconnect.interviewservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hireconnect.interviewservice.client.ApplicationServiceClient;
import com.hireconnect.interviewservice.client.dto.ApplicationSummaryDto;
import com.hireconnect.interviewservice.dto.request.InterviewScheduleRequestDto;
import com.hireconnect.interviewservice.dto.response.InterviewResponseDto;
import com.hireconnect.interviewservice.entity.Interview;
import com.hireconnect.interviewservice.enums.InterviewStatus;
import com.hireconnect.interviewservice.enums.Role;
import com.hireconnect.interviewservice.event.NotificationEvent;
import com.hireconnect.interviewservice.exception.BadRequestException;
import com.hireconnect.interviewservice.exception.ResourceNotFoundException;
import com.hireconnect.interviewservice.exception.UnauthorizedException;
import com.hireconnect.interviewservice.producer.NotificationEventProducer;
import com.hireconnect.interviewservice.repository.InterviewRepository;
import com.hireconnect.interviewservice.security.AuthenticatedUser;

import lombok.Builder;

@ExtendWith(MockitoExtension.class)
@Builder
public class InterviewServiceImplTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private ApplicationServiceClient applicationServiceClient;

    @Mock
    private NotificationEventProducer notificationEventProducer;

    @InjectMocks
    private InterviewServiceImpl interviewService;

    private AuthenticatedUser recruiterUser;
    private AuthenticatedUser candidateUser;
    private InterviewScheduleRequestDto scheduleRequestDto;
    private ApplicationSummaryDto applicationSummary;
    private Interview interview;

    @BeforeEach
    void setUp() {
        recruiterUser = new AuthenticatedUser(2L, "recruiter@example.com", Role.RECRUITER);
        candidateUser = new AuthenticatedUser(1L, "candidate@example.com", Role.CANDIDATE);

        scheduleRequestDto = new InterviewScheduleRequestDto();
        scheduleRequestDto.setApplicationId(10L);

        applicationSummary = new ApplicationSummaryDto();
        applicationSummary.setId(10L);
        applicationSummary.setJobId(100L);
        applicationSummary.setCandidateId(1L);
        applicationSummary.setCandidateEmail("candidate@example.com");
        applicationSummary.setRecruiterId(2L);
        applicationSummary.setStatus("SHORTLISTED");

        interview = Interview.builder()
                .id(50L)
                .applicationId(10L)
                .jobId(100L)
                .candidateId(1L)
                .recruiterId(2L)
                .status(InterviewStatus.SCHEDULED)
                .build();
    }

    @Test
    void scheduleInterview_Success() {
        when(applicationServiceClient.getApplicationSummary(10L)).thenReturn(applicationSummary);
        when(interviewRepository.save(any(Interview.class))).thenReturn(interview);

        InterviewResponseDto response = interviewService.scheduleInterview(recruiterUser, scheduleRequestDto);

        assertNotNull(response);
        assertEquals(50L, response.getId());
        assertEquals(InterviewStatus.SCHEDULED, response.getStatus());
        verify(notificationEventProducer, times(1)).sendNotification(any(NotificationEvent.class));
    }

    @Test
    void scheduleInterview_ApplicationNotFound_ThrowsException() {
        when(applicationServiceClient.getApplicationSummary(10L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> 
            interviewService.scheduleInterview(recruiterUser, scheduleRequestDto));
    }

    @Test
    void scheduleInterview_Unauthorized_ThrowsException() {
        applicationSummary.setRecruiterId(99L);
        when(applicationServiceClient.getApplicationSummary(10L)).thenReturn(applicationSummary);

        assertThrows(UnauthorizedException.class, () -> 
            interviewService.scheduleInterview(recruiterUser, scheduleRequestDto));
    }

    @Test
    void scheduleInterview_NotShortlisted_ThrowsException() {
        applicationSummary.setStatus("APPLIED");
        when(applicationServiceClient.getApplicationSummary(10L)).thenReturn(applicationSummary);

        assertThrows(BadRequestException.class, () -> 
            interviewService.scheduleInterview(recruiterUser, scheduleRequestDto));
    }

    @Test
    void getRecruiterInterviews_Success() {
        when(interviewRepository.findByRecruiterIdOrderByScheduledAtDesc(2L))
            .thenReturn(List.of(interview));

        List<InterviewResponseDto> responses = interviewService.getRecruiterInterviews(recruiterUser);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(50L, responses.get(0).getId());
    }

    @Test
    void cancelInterview_Success() {
        when(interviewRepository.findByIdAndRecruiterId(50L, 2L)).thenReturn(Optional.of(interview));
        when(interviewRepository.save(any(Interview.class))).thenReturn(interview);

        InterviewResponseDto response = interviewService.cancelInterview(recruiterUser, 50L);

        assertNotNull(response);
        assertEquals(InterviewStatus.CANCELLED, interview.getStatus());
        verify(notificationEventProducer, times(1)).sendNotification(any(NotificationEvent.class));
    }

    @Test
    void cancelInterview_NotFound_ThrowsException() {
        when(interviewRepository.findByIdAndRecruiterId(50L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            interviewService.cancelInterview(recruiterUser, 50L));
    }
}
