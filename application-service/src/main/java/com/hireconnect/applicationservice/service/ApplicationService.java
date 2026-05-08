package com.hireconnect.applicationservice.service;

import java.util.List;

import com.hireconnect.applicationservice.client.dto.ApplicationSummaryDto;
import com.hireconnect.applicationservice.dto.request.ApplicationRequestDto;
import com.hireconnect.applicationservice.dto.request.ApplicationStatusUpdateRequestDto;
import com.hireconnect.applicationservice.dto.response.ApplicationResponseDto;
import com.hireconnect.applicationservice.dto.response.RecruiterJobApplicationResponseDto;
import com.hireconnect.applicationservice.security.AuthenticatedUser;

// [Disha Gujar] : Service interface defining the business logic contract for job application management.
// Covers candidate application submission, status updates by recruiters, application retrieval
// by candidate or recruiter, cross-service summary lookups, and duplicate-application detection.
public interface ApplicationService {

    ApplicationResponseDto applyToJob(AuthenticatedUser user, ApplicationRequestDto requestDto);

    List<ApplicationResponseDto> getMyApplications(AuthenticatedUser user);

    ApplicationResponseDto getMyApplicationById(AuthenticatedUser user, Long applicationId);

    ApplicationResponseDto withdrawMyApplication(AuthenticatedUser user, Long applicationId);

    List<ApplicationResponseDto> getApplicationsForRecruiter(AuthenticatedUser user);

    ApplicationResponseDto updateApplicationStatus(
            AuthenticatedUser user,
            Long applicationId,
            ApplicationStatusUpdateRequestDto requestDto
    );

    ApplicationSummaryDto getApplicationSummary(Long applicationId);
    
    List<ApplicationResponseDto> getApplicationsByJobId(AuthenticatedUser user, Long jobId);
    
    List<RecruiterJobApplicationResponseDto> getApplicationsForRecruiterJob(
            AuthenticatedUser user,
            Long jobId
    );
    Boolean hasCandidateAppliedToJob(Long candidateId, Long jobId);

    List<ApplicationResponseDto> getApplicationsByRecruiterIdInternal(Long recruiterId);

    List<ApplicationResponseDto> getAllApplicationsInternal();

    Boolean isCandidateShortlistedForRecruiterJobInternal(Long recruiterId, Long candidateId, Long jobId);
}
