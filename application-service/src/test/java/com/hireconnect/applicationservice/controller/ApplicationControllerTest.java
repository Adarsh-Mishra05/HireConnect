package com.hireconnect.applicationservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hireconnect.applicationservice.client.dto.ApplicationSummaryDto;
import com.hireconnect.applicationservice.dto.request.ApplicationRequestDto;
import com.hireconnect.applicationservice.dto.request.ApplicationStatusUpdateRequestDto;
import com.hireconnect.applicationservice.dto.response.ApplicationResponseDto;
import com.hireconnect.applicationservice.dto.response.RecruiterJobApplicationResponseDto;
import com.hireconnect.applicationservice.enums.ApplicationStatus;
import com.hireconnect.applicationservice.enums.Role;
import com.hireconnect.applicationservice.security.AuthenticatedUser;
import com.hireconnect.applicationservice.service.ApplicationService;

@ExtendWith(MockitoExtension.class)
public class ApplicationControllerTest {

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private ApplicationController applicationController;

    private AuthenticatedUser candidateUser;
    private AuthenticatedUser recruiterUser;
    private ApplicationResponseDto applicationResponseDto;

    @BeforeEach
    void setUp() {
        candidateUser = new AuthenticatedUser(1L, "candidate@example.com", Role.CANDIDATE);
        recruiterUser = new AuthenticatedUser(2L, "recruiter@example.com", Role.RECRUITER);

        applicationResponseDto = ApplicationResponseDto.builder()
                .id(10L)
                .jobId(100L)
                .candidateId(1L)
                .recruiterId(2L)
                .status(ApplicationStatus.APPLIED)
                .build();
    }

    @Test
    void applyToJob_ShouldReturnCreatedStatus() {
        ApplicationRequestDto requestDto = new ApplicationRequestDto();
        requestDto.setJobId(100L);

        when(applicationService.applyToJob(eq(candidateUser), any(ApplicationRequestDto.class)))
                .thenReturn(applicationResponseDto);

        ResponseEntity<ApplicationResponseDto> response = applicationController.applyToJob(candidateUser, requestDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(10L, response.getBody().getId());
    }

    @Test
    void getMyApplications_ShouldReturnList() {
        when(applicationService.getMyApplications(candidateUser))
                .thenReturn(List.of(applicationResponseDto));

        ResponseEntity<List<ApplicationResponseDto>> response = applicationController.getMyApplications(candidateUser);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getMyApplicationById_ShouldReturnApplication() {
        when(applicationService.getMyApplicationById(candidateUser, 10L))
                .thenReturn(applicationResponseDto);

        ResponseEntity<ApplicationResponseDto> response = applicationController.getMyApplicationById(candidateUser, 10L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody().getId());
    }

    @Test
    void updateApplicationStatus_ShouldReturnUpdatedApplication() {
        ApplicationStatusUpdateRequestDto updateDto = new ApplicationStatusUpdateRequestDto();
        updateDto.setStatus(ApplicationStatus.SHORTLISTED);

        ApplicationResponseDto updatedResponse = ApplicationResponseDto.builder()
                .id(10L)
                .status(ApplicationStatus.SHORTLISTED)
                .build();

        when(applicationService.updateApplicationStatus(eq(recruiterUser), eq(10L), any(ApplicationStatusUpdateRequestDto.class)))
                .thenReturn(updatedResponse);

        ResponseEntity<ApplicationResponseDto> response = applicationController.updateApplicationStatus(recruiterUser, 10L, updateDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ApplicationStatus.SHORTLISTED, response.getBody().getStatus());
    }

    @Test
    void getApplicationSummary_ShouldReturnSummary() {
        ApplicationSummaryDto summaryDto = ApplicationSummaryDto.builder().id(10L).build();
        when(applicationService.getApplicationSummary(10L)).thenReturn(summaryDto);

        ResponseEntity<ApplicationSummaryDto> response = applicationController.getApplicationSummary(10L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody().getId());
    }
}
