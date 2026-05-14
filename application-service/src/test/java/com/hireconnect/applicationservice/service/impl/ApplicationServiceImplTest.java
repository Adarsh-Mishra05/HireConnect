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
import com.hireconnect.applicationservice.client.dto.ProfileResponseDto;
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
    private ProfileResponseDto completeProfile;

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

        completeProfile = new ProfileResponseDto();
        completeProfile.setUserId(1L);
        completeProfile.setFirstName("John");
        completeProfile.setPhone("9999999999");
    }

    @Test
    void applyToJob_Success() {
        when(profileServiceClient.getMyProfile()).thenReturn(completeProfile);
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
        when(profileServiceClient.getMyProfile()).thenReturn(completeProfile);
        when(jobServiceClient.doesJobExist(100L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> 
            applicationService.applyToJob(candidateUser, applicationRequestDto));
    }

    @Test
    void applyToJob_JobNotOpen_ThrowsException() {
        when(profileServiceClient.getMyProfile()).thenReturn(completeProfile);
        when(jobServiceClient.doesJobExist(100L)).thenReturn(true);
        when(jobServiceClient.isJobOpen(100L)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> 
            applicationService.applyToJob(candidateUser, applicationRequestDto));
    }

    @Test
    void applyToJob_AlreadyApplied_ThrowsException() {
        when(profileServiceClient.getMyProfile()).thenReturn(completeProfile);
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

    @Test
    void getMyApplicationById_Success() {
        when(jobApplicationRepository.findByIdAndCandidateId(10L, 1L)).thenReturn(Optional.of(jobApplication));
        ApplicationResponseDto response = applicationService.getMyApplicationById(candidateUser, 10L);
        assertEquals(10L, response.getId());
    }

    @Test
    void withdrawMyApplication_Success() {
        when(jobApplicationRepository.findByIdAndCandidateId(10L, 1L)).thenReturn(Optional.of(jobApplication));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(jobApplication);
        ApplicationResponseDto response = applicationService.withdrawMyApplication(candidateUser, 10L);
        assertNotNull(response);
        verify(notificationEventProducer, times(2)).sendNotification(any(NotificationEvent.class));
    }

    @Test
    void withdrawMyApplication_AlreadyWithdrawn_ThrowsException() {
        jobApplication.setStatus(ApplicationStatus.WITHDRAWN);
        when(jobApplicationRepository.findByIdAndCandidateId(10L, 1L)).thenReturn(Optional.of(jobApplication));
        assertThrows(BadRequestException.class, () -> applicationService.withdrawMyApplication(candidateUser, 10L));
    }

    @Test
    void withdrawMyApplication_Accepted_ThrowsException() {
        jobApplication.setStatus(ApplicationStatus.ACCEPTED);
        when(jobApplicationRepository.findByIdAndCandidateId(10L, 1L)).thenReturn(Optional.of(jobApplication));
        assertThrows(BadRequestException.class, () -> applicationService.withdrawMyApplication(candidateUser, 10L));
    }

    @Test
    void getApplicationsForRecruiter_EmptyWhenNoJobs() {
        when(jobServiceClient.getJobIdsByRecruiter(2L)).thenReturn(List.of());
        List<ApplicationResponseDto> responses = applicationService.getApplicationsForRecruiter(recruiterUser);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getApplicationsForRecruiter_Success() {
        when(jobServiceClient.getJobIdsByRecruiter(2L)).thenReturn(List.of(100L));
        when(jobApplicationRepository.findByJobIdInOrderByAppliedAtDesc(List.of(100L)))
                .thenReturn(List.of(jobApplication));
        List<ApplicationResponseDto> responses = applicationService.getApplicationsForRecruiter(recruiterUser);
        assertEquals(1, responses.size());
    }

    @Test
    void updateApplicationStatus_AppliedNotAllowed_ThrowsException() {
        ApplicationStatusUpdateRequestDto updateDto = new ApplicationStatusUpdateRequestDto();
        updateDto.setStatus(ApplicationStatus.APPLIED);
        assertThrows(BadRequestException.class,
                () -> applicationService.updateApplicationStatus(recruiterUser, 10L, updateDto));
    }

    @Test
    void getApplicationSummary_Success() {
        when(jobApplicationRepository.findById(10L)).thenReturn(Optional.of(jobApplication));
        assertEquals(10L, applicationService.getApplicationSummary(10L).getId());
    }

    @Test
    void getApplicationsByJobId_Success() {
        when(jobServiceClient.isJobOwnedByRecruiter(100L, 2L)).thenReturn(true);
        when(jobApplicationRepository.findByJobIdOrderByAppliedAtDesc(100L)).thenReturn(List.of(jobApplication));
        List<ApplicationResponseDto> responses = applicationService.getApplicationsByJobId(recruiterUser, 100L);
        assertEquals(1, responses.size());
    }

    @Test
    void getApplicationsByJobId_Unauthorized_ThrowsException() {
        when(jobServiceClient.isJobOwnedByRecruiter(100L, 2L)).thenReturn(false);
        assertThrows(UnauthorizedException.class,
                () -> applicationService.getApplicationsByJobId(recruiterUser, 100L));
    }

    @Test
    void getApplicationsForRecruiterJob_Success() {
        when(jobServiceClient.isJobOwnedByRecruiter(100L, 2L)).thenReturn(true);
        when(jobApplicationRepository.findByJobIdOrderByAppliedAtDesc(100L)).thenReturn(List.of(jobApplication));
        when(profileServiceClient.getCandidateProfilePreview("2", "recruiter@example.com", "RECRUITER", 1L))
                .thenReturn(null);
        assertEquals(1, applicationService.getApplicationsForRecruiterJob(recruiterUser, 100L).size());
    }

    @Test
    void getApplicationsForRecruiterJob_Unauthorized_ThrowsException() {
        when(jobServiceClient.isJobOwnedByRecruiter(100L, 2L)).thenReturn(false);
        assertThrows(UnauthorizedException.class,
                () -> applicationService.getApplicationsForRecruiterJob(recruiterUser, 100L));
    }

    @Test
    void internalAndCheckMethods_Success() {
        when(jobApplicationRepository.findByRecruiterIdOrderByAppliedAtDesc(2L)).thenReturn(List.of(jobApplication));
        when(jobApplicationRepository.findAllByOrderByAppliedAtDesc()).thenReturn(List.of(jobApplication));
        when(jobApplicationRepository.findByJobIdAndCandidateId(100L, 1L)).thenReturn(Optional.of(jobApplication));
        when(jobApplicationRepository.existsByCandidateIdAndJobId(1L, 100L)).thenReturn(true);

        assertEquals(1, applicationService.getApplicationsByRecruiterIdInternal(2L).size());
        assertEquals(1, applicationService.getAllApplicationsInternal().size());
        assertFalse(applicationService.isCandidateShortlistedForRecruiterJobInternal(2L, 1L, 100L));
        jobApplication.setStatus(ApplicationStatus.SHORTLISTED);
        assertTrue(applicationService.isCandidateShortlistedForRecruiterJobInternal(2L, 1L, 100L));
        assertTrue(applicationService.hasCandidateAppliedToJob(1L, 100L));
    }
}
