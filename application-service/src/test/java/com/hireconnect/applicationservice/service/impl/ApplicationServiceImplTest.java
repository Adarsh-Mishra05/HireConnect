package com.hireconnect.applicationservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.hireconnect.applicationservice.client.JobServiceClient;
import com.hireconnect.applicationservice.client.ProfileServiceClient;
import com.hireconnect.applicationservice.dto.request.ApplicationRequestDto;
import com.hireconnect.applicationservice.dto.request.ApplicationStatusUpdateRequestDto;
import com.hireconnect.applicationservice.dto.response.ApplicationResponseDto;
import com.hireconnect.applicationservice.entity.JobApplication;
import com.hireconnect.applicationservice.enums.ApplicationStatus;
import com.hireconnect.applicationservice.enums.Role;
import com.hireconnect.applicationservice.event.NotificationEvent;
import com.hireconnect.applicationservice.exception.BadRequestException;
import com.hireconnect.applicationservice.exception.ResourceNotFoundException;
import com.hireconnect.applicationservice.exception.UnauthorizedException;
import com.hireconnect.applicationservice.producer.NotificationEventProducer;
import com.hireconnect.applicationservice.repository.JobApplicationRepository;
import com.hireconnect.applicationservice.security.AuthenticatedUser;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceImplTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private JobServiceClient jobServiceClient;

    @Mock
    private NotificationEventProducer notificationEventProducer;

    @Mock
    private ProfileServiceClient profileServiceClient;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private AuthenticatedUser candidateUser;
    private AuthenticatedUser recruiterUser;
    private ApplicationRequestDto applicationRequestDto;
    private JobApplication jobApplication;

    @BeforeEach
    void setUp() {
        candidateUser = new AuthenticatedUser(1L, "candidate@example.com", Role.CANDIDATE);
        recruiterUser = new AuthenticatedUser(2L, "recruiter@example.com", Role.RECRUITER);

        applicationRequestDto = new ApplicationRequestDto();
        applicationRequestDto.setJobId(100L);
        applicationRequestDto.setResumeUrl("http://resume.com");

        jobApplication = JobApplication.builder()
                .id(10L)
                .jobId(100L)
                .candidateId(1L)
                .candidateEmail("candidate@example.com")
                .recruiterId(2L)
                .status(ApplicationStatus.APPLIED)
                .build();
    }

    @Test
    void applyToJob_Success() {
        when(jobServiceClient.doesJobExist(100L)).thenReturn(true);
        when(jobServiceClient.isJobOpen(100L)).thenReturn(true);
        when(jobApplicationRepository.existsByJobIdAndCandidateId(100L, 1L)).thenReturn(false);
        when(jobServiceClient.getRecruiterIdByJobId(100L)).thenReturn(2L);
        when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(jobApplication);

        ApplicationResponseDto response = applicationService.applyToJob(candidateUser, applicationRequestDto);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(ApplicationStatus.APPLIED, response.getStatus());

        verify(notificationEventProducer, times(2)).sendNotification(any(NotificationEvent.class));
    }

    @Test
    void applyToJob_JobNotFound_ThrowsException() {
        when(jobServiceClient.doesJobExist(100L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> 
            applicationService.applyToJob(candidateUser, applicationRequestDto));
    }

    @Test
    void applyToJob_JobNotOpen_ThrowsException() {
        when(jobServiceClient.doesJobExist(100L)).thenReturn(true);
        when(jobServiceClient.isJobOpen(100L)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> 
            applicationService.applyToJob(candidateUser, applicationRequestDto));
    }

    @Test
    void applyToJob_AlreadyApplied_ThrowsException() {
        when(jobServiceClient.doesJobExist(100L)).thenReturn(true);
        when(jobServiceClient.isJobOpen(100L)).thenReturn(true);
        when(jobApplicationRepository.existsByJobIdAndCandidateId(100L, 1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> 
            applicationService.applyToJob(candidateUser, applicationRequestDto));
    }

    @Test
    void updateApplicationStatus_Success() {
        ApplicationStatusUpdateRequestDto updateDto = new ApplicationStatusUpdateRequestDto();
        updateDto.setStatus(ApplicationStatus.SHORTLISTED);

        when(jobApplicationRepository.findById(10L)).thenReturn(Optional.of(jobApplication));
        when(jobServiceClient.isJobOwnedByRecruiter(100L, 2L)).thenReturn(true);
        when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(jobApplication);

        ApplicationResponseDto response = applicationService.updateApplicationStatus(recruiterUser, 10L, updateDto);

        assertEquals(ApplicationStatus.SHORTLISTED, response.getStatus());
        verify(notificationEventProducer, times(1)).sendNotification(any(NotificationEvent.class));
    }

    @Test
    void updateApplicationStatus_Unauthorized_ThrowsException() {
        ApplicationStatusUpdateRequestDto updateDto = new ApplicationStatusUpdateRequestDto();
        updateDto.setStatus(ApplicationStatus.SHORTLISTED);

        when(jobApplicationRepository.findById(10L)).thenReturn(Optional.of(jobApplication));
        when(jobServiceClient.isJobOwnedByRecruiter(100L, 2L)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> 
            applicationService.updateApplicationStatus(recruiterUser, 10L, updateDto));
    }

    @Test
    void getMyApplications_Success() {
        when(jobApplicationRepository.findByCandidateIdOrderByAppliedAtDesc(1L))
            .thenReturn(List.of(jobApplication));

        List<ApplicationResponseDto> responses = applicationService.getMyApplications(candidateUser);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(10L, responses.get(0).getId());
    }
}
