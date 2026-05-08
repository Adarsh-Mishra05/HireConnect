package com.hireconnect.applicationservice.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hireconnect.applicationservice.client.JobServiceClient;
import com.hireconnect.applicationservice.client.ProfileServiceClient;
import com.hireconnect.applicationservice.client.dto.ProfileResponseDto;
import com.hireconnect.applicationservice.client.dto.ApplicationSummaryDto;
import com.hireconnect.applicationservice.client.dto.CandidateProfilePreviewDto;
import com.hireconnect.applicationservice.dto.request.ApplicationRequestDto;
import com.hireconnect.applicationservice.dto.request.ApplicationStatusUpdateRequestDto;
import com.hireconnect.applicationservice.dto.response.ApplicationResponseDto;
import com.hireconnect.applicationservice.dto.response.RecruiterJobApplicationResponseDto;
import com.hireconnect.applicationservice.entity.JobApplication;
import com.hireconnect.applicationservice.enums.ApplicationStatus;
import com.hireconnect.applicationservice.enums.NotificationType;
import com.hireconnect.applicationservice.enums.Role;
import com.hireconnect.applicationservice.event.NotificationEvent;
import com.hireconnect.applicationservice.exception.BadRequestException;
import com.hireconnect.applicationservice.exception.ResourceNotFoundException;
import com.hireconnect.applicationservice.exception.UnauthorizedException;
import com.hireconnect.applicationservice.producer.NotificationEventProducer;
import com.hireconnect.applicationservice.repository.JobApplicationRepository;
import com.hireconnect.applicationservice.security.AuthenticatedUser;
import com.hireconnect.applicationservice.service.ApplicationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    private final JobApplicationRepository jobApplicationRepository;
    private final JobServiceClient jobServiceClient;
    private final NotificationEventProducer notificationEventProducer;
    private final ProfileServiceClient profileServiceClient;

    @Override
    @Transactional
    public ApplicationResponseDto applyToJob(AuthenticatedUser user, ApplicationRequestDto requestDto) {

        log.info("Application submission started for candidateId: {}, jobId: {}",
                user != null ? user.getUserId() : null, requestDto.getJobId());

        validateCandidate(user);
        validateCandidateProfileCompleteness(user);

        log.info("Checking if job exists for jobId: {}", requestDto.getJobId());
        Boolean jobExists = jobServiceClient.doesJobExist(requestDto.getJobId());
        if (Boolean.FALSE.equals(jobExists)) {
            throw new ResourceNotFoundException("Job not found with id: " + requestDto.getJobId());
        }

        log.info("Checking if job is open for jobId: {}", requestDto.getJobId());
        Boolean jobOpen = jobServiceClient.isJobOpen(requestDto.getJobId());
        if (Boolean.FALSE.equals(jobOpen)) {
            throw new BadRequestException("You can only apply to open jobs");
        }

        boolean alreadyApplied = jobApplicationRepository.existsByJobIdAndCandidateId(
                requestDto.getJobId(),
                user.getUserId()
        );
        log.info("Checked duplicate application for candidateId: {}, jobId: {}, alreadyApplied: {}",
                user.getUserId(), requestDto.getJobId(), alreadyApplied);

        if (alreadyApplied) {
            throw new BadRequestException("You have already applied to this job");
        }

        log.info("Fetching recruiter id for jobId: {}", requestDto.getJobId());
        Long recruiterId = jobServiceClient.getRecruiterIdByJobId(requestDto.getJobId());

        JobApplication application = JobApplication.builder()
                .jobId(requestDto.getJobId())
                .candidateId(user.getUserId())
                .candidateEmail(user.getEmail())
                .recruiterId(recruiterId)
                .resumeUrl(requestDto.getResumeUrl())
                .coverLetter(requestDto.getCoverLetter())
                .status(ApplicationStatus.APPLIED)
                .build();

        try {
            JobApplication savedApplication = jobApplicationRepository.save(application);
            log.info("Application submitted successfully with applicationId: {}, candidateId: {}, jobId: {}",
                    savedApplication.getId(), savedApplication.getCandidateId(), savedApplication.getJobId());

            sendApplicationSubmittedNotifications(savedApplication, user);
            return mapToResponse(savedApplication);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Application submission failed due to duplicate entry for candidateId: {}, jobId: {}",
                    user.getUserId(), requestDto.getJobId());
            throw new BadRequestException("You have already applied to this job");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getMyApplications(AuthenticatedUser user) {

        log.info("Fetching applications for candidateId: {}", user != null ? user.getUserId() : null);

        validateCandidate(user);

        List<ApplicationResponseDto> applications = jobApplicationRepository
                .findByCandidateIdOrderByAppliedAtDesc(user.getUserId())
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Fetched {} applications for candidateId: {}", applications.size(), user.getUserId());
        return applications;
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponseDto getMyApplicationById(AuthenticatedUser user, Long applicationId) {

        log.info("Fetching applicationId: {} for candidateId: {}",
                applicationId, user != null ? user.getUserId() : null);

        validateCandidate(user);

        JobApplication application = jobApplicationRepository.findByIdAndCandidateId(applicationId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        log.info("Application found for candidateId: {}, applicationId: {}", user.getUserId(), applicationId);
        return mapToResponse(application);
    }

    @Override
    @Transactional
    public ApplicationResponseDto withdrawMyApplication(AuthenticatedUser user, Long applicationId) {
        log.info("Application withdrawal started for candidateId: {}, applicationId: {}",
                user != null ? user.getUserId() : null, applicationId);

        validateCandidate(user);

        JobApplication application = jobApplicationRepository.findByIdAndCandidateId(applicationId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new BadRequestException("Application is already withdrawn");
        }

        if (application.getStatus() == ApplicationStatus.ACCEPTED) {
            throw new BadRequestException("Accepted applications cannot be withdrawn");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        JobApplication updatedApplication = jobApplicationRepository.save(application);

        sendApplicationWithdrawalNotifications(updatedApplication, user);
        log.info("Application withdrawn successfully for applicationId: {}", updatedApplication.getId());
        return mapToResponse(updatedApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getApplicationsForRecruiter(AuthenticatedUser user) {

        log.info("Fetching all applications for recruiterId: {}", user != null ? user.getUserId() : null);

        validateRecruiter(user);

        List<Long> recruiterJobIds = jobServiceClient.getJobIdsByRecruiter(user.getUserId());
        log.info("Fetched {} job ids for recruiterId: {}",
                recruiterJobIds != null ? recruiterJobIds.size() : 0, user.getUserId());

        if (recruiterJobIds == null || recruiterJobIds.isEmpty()) {
            log.info("No jobs found for recruiterId: {}. Returning empty applications list.", user.getUserId());
            return List.of();
        }

        List<ApplicationResponseDto> applications = jobApplicationRepository
                .findByJobIdInOrderByAppliedAtDesc(recruiterJobIds)
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Fetched {} applications for recruiterId: {}", applications.size(), user.getUserId());
        return applications;
    }

    @Override
    @Transactional
    public ApplicationResponseDto updateApplicationStatus(
            AuthenticatedUser user,
            Long applicationId,
            ApplicationStatusUpdateRequestDto requestDto
    ) {

        log.info("Application status update started for recruiterId: {}, applicationId: {}, newStatus: {}",
                user != null ? user.getUserId() : null, applicationId, requestDto.getStatus());

        validateRecruiter(user);

        if (requestDto.getStatus() == ApplicationStatus.APPLIED) {
            throw new BadRequestException("Status cannot be changed back to APPLIED");
        }

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        log.info("Checking ownership for recruiterId: {}, jobId: {}", user.getUserId(), application.getJobId());
        Boolean isOwner = jobServiceClient.isJobOwnedByRecruiter(application.getJobId(), user.getUserId());
        if (Boolean.FALSE.equals(isOwner)) {
            throw new UnauthorizedException("You can update applications only for your own jobs");
        }

        application.setStatus(requestDto.getStatus());

        JobApplication updatedApplication = jobApplicationRepository.save(application);
        log.info("Application status updated successfully for applicationId: {}, newStatus: {}",
                updatedApplication.getId(), updatedApplication.getStatus());

        sendApplicationStatusUpdateNotification(updatedApplication);
        return mapToResponse(updatedApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationSummaryDto getApplicationSummary(Long applicationId) {
        log.info("Fetching application summary for applicationId: {}", applicationId);

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        log.info("Application summary prepared for applicationId: {}", application.getId());
        return ApplicationSummaryDto.builder()
                .id(application.getId())
                .jobId(application.getJobId())
                .candidateId(application.getCandidateId())
                .recruiterId(application.getRecruiterId())
                .candidateEmail(application.getCandidateEmail())
                .status(application.getStatus().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getApplicationsByRecruiterIdInternal(Long recruiterId) {
        return jobApplicationRepository.findByRecruiterIdOrderByAppliedAtDesc(recruiterId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getAllApplicationsInternal() {
        return jobApplicationRepository.findAllByOrderByAppliedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isCandidateShortlistedForRecruiterJobInternal(Long recruiterId, Long candidateId, Long jobId) {
        return jobApplicationRepository.findByJobIdAndCandidateId(jobId, candidateId)
                .map(application ->
                        application.getRecruiterId().equals(recruiterId)
                                && application.getStatus() == ApplicationStatus.SHORTLISTED)
                .orElse(false);
    }

    private void validateCandidate(AuthenticatedUser user) {
        if (user == null || user.getRole() != Role.CANDIDATE) {
            log.warn("Unauthorized candidate action attempted. userId: {}, role: {}",
                    user != null ? user.getUserId() : null,
                    user != null ? user.getRole() : null);
            throw new UnauthorizedException("Only candidates can perform this action");
        }
    }

    private void validateCandidateProfileCompleteness(AuthenticatedUser user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BadRequestException("Please complete your profile with name, phone number, and email before applying");
        }

        try {
            ProfileResponseDto profile = profileServiceClient.getMyProfile();
            boolean missingName = profile == null
                    || profile.getFirstName() == null
                    || profile.getFirstName().isBlank();
            boolean missingPhone = profile == null
                    || profile.getPhone() == null
                    || profile.getPhone().isBlank();

            if (missingName || missingPhone) {
                throw new BadRequestException("Please complete your profile with name, phone number, and email before applying");
            }
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Candidate profile completeness check failed for userId={}: {}", user.getUserId(), ex.getMessage());
            throw new BadRequestException("Please complete your profile with name, phone number, and email before applying");
        }
    }

    private void validateRecruiter(AuthenticatedUser user) {
        if (user == null || user.getRole() != Role.RECRUITER) {
            log.warn("Unauthorized recruiter action attempted. userId: {}, role: {}",
                    user != null ? user.getUserId() : null,
                    user != null ? user.getRole() : null);
            throw new UnauthorizedException("Only recruiters can perform this action");
        }
    }

    private ApplicationResponseDto mapToResponse(JobApplication application) {
        return ApplicationResponseDto.builder()
                .id(application.getId())
                .jobId(application.getJobId())
                .candidateId(application.getCandidateId())
                .recruiterId(application.getRecruiterId())
                .status(application.getStatus())
                .resumeUrl(application.getResumeUrl())
                .coverLetter(application.getCoverLetter())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }

    private void sendApplicationSubmittedNotifications(JobApplication application, AuthenticatedUser user) {

        log.info("Sending application submitted notifications for applicationId: {}, candidateId: {}, recruiterId: {}",
                application.getId(), application.getCandidateId(), application.getRecruiterId());

        String message = "Dear Candidate,\n\n"
                + "Greetings from HireConnect.\n\n"
                + "We are pleased to inform you that your application has been successfully submitted.\n\n"
                + "Application Details:\n"
                + "Job ID: " + application.getJobId() + "\n"
                + "Application ID: " + application.getId() + "\n\n"
                + "Our recruitment team will review your profile, and you will be notified about the next steps.\n\n"
                + "We wish you the best of luck in your application.\n\n"
                + "Regards,\n"
                + "Recruitment Team\n"
                + "HireConnect";

        NotificationEvent candidateEvent = NotificationEvent.builder()
                .recipientUserId(application.getCandidateId())
                .recipientEmail(application.getCandidateEmail())
                .title("Application Submitted Successfully")
                .message(message)
                .type(NotificationType.APPLICATION)
                .sendEmail(true)
                .build();

        notificationEventProducer.sendNotification(candidateEvent);

        NotificationEvent recruiterEvent = NotificationEvent.builder()
                .recipientUserId(application.getRecruiterId())
                .recipientEmail(null)
                .title("New Application Received")
                .message("A candidate has applied for your job ID " + application.getJobId() + ".")
                .type(NotificationType.APPLICATION)
                .sendEmail(false)
                .build();

        notificationEventProducer.sendNotification(recruiterEvent);

        log.info("Application submitted notifications sent for applicationId: {}", application.getId());
    }

    private void sendApplicationStatusUpdateNotification(JobApplication application) {
        log.info("Sending application status update notification for applicationId: {}, candidateId: {}, status: {}",
                application.getId(), application.getCandidateId(), application.getStatus());

        NotificationEvent statusEvent = NotificationEvent.builder()
                .recipientUserId(application.getCandidateId())
                .recipientEmail(null)
                .title("Application Status Updated")
                .message("Your application status for job ID " + application.getJobId() + " is now "
                        + application.getStatus().name() + ".")
                .type(NotificationType.APPLICATION)
                .sendEmail(false)
                .build();

        notificationEventProducer.sendNotification(statusEvent);

        log.info("Application status update notification sent for applicationId: {}", application.getId());
    }

    private void sendApplicationWithdrawalNotifications(JobApplication application, AuthenticatedUser user) {
        NotificationEvent candidateEvent = NotificationEvent.builder()
                .recipientUserId(application.getCandidateId())
                .recipientEmail(user.getEmail())
                .title("Application Withdrawn")
                .message("You have successfully withdrawn your application for job ID "
                        + application.getJobId() + ".")
                .type(NotificationType.APPLICATION)
                .sendEmail(true)
                .build();
        notificationEventProducer.sendNotification(candidateEvent);

        NotificationEvent recruiterEvent = NotificationEvent.builder()
                .recipientUserId(application.getRecruiterId())
                .recipientEmail(null)
                .title("Candidate Withdrew Application")
                .message("A candidate withdrew an application for your job ID " + application.getJobId() + ".")
                .type(NotificationType.APPLICATION)
                .sendEmail(false)
                .build();
        notificationEventProducer.sendNotification(recruiterEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getApplicationsByJobId(AuthenticatedUser user, Long jobId) {

        log.info("Fetching applications by jobId: {} for recruiterId: {}",
                jobId, user != null ? user.getUserId() : null);

        validateRecruiter(user);

        Boolean isOwner = jobServiceClient.isJobOwnedByRecruiter(jobId, user.getUserId());
        log.info("Ownership check completed for recruiterId: {}, jobId: {}, isOwner: {}",
                user.getUserId(), jobId, isOwner);

        if (Boolean.FALSE.equals(isOwner)) {
            throw new UnauthorizedException("You can view applications only for your own jobs");
        }

        List<ApplicationResponseDto> applications = jobApplicationRepository
                .findByJobIdOrderByAppliedAtDesc(jobId)
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Fetched {} applications for jobId: {}", applications.size(), jobId);
        return applications;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecruiterJobApplicationResponseDto> getApplicationsForRecruiterJob(
            AuthenticatedUser user,
            Long jobId
    ) {
        log.info("Fetching recruiter job applications with candidate preview for recruiterId: {}, jobId: {}",
                user != null ? user.getUserId() : null, jobId);

        validateRecruiter(user);

        Boolean ownsJob = jobServiceClient.isJobOwnedByRecruiter(jobId, user.getUserId());
        log.info("Recruiter ownership check for preview completed. recruiterId: {}, jobId: {}, ownsJob: {}",
                user.getUserId(), jobId, ownsJob);

        if (ownsJob == null || !ownsJob) {
            log.warn("Recruiter is not authorized to view applications. recruiterId: {}, jobId: {}",
                    user.getUserId(), jobId);
            throw new UnauthorizedException("You are not authorized to view applications for this job");
        }

        List<RecruiterJobApplicationResponseDto> applications =
                jobApplicationRepository.findByJobIdOrderByAppliedAtDesc(jobId)
                        .stream()
                        .map(application -> {
                            CandidateProfilePreviewDto candidateProfile =
                                    profileServiceClient.getCandidateProfilePreview(
                                            String.valueOf(user.getUserId()),
                                            user.getEmail(),
                                            user.getRole().name(),
                                            application.getCandidateId()
                                    );

                            return RecruiterJobApplicationResponseDto.builder()
                                    .applicationId(application.getId())
                                    .candidateId(application.getCandidateId())
                                    .jobId(application.getJobId())
                                    .status(application.getStatus())
                                    .appliedAt(application.getAppliedAt())
                                    .candidateProfile(candidateProfile)
                                    .build();
                        })
                        .toList();

        log.info("Fetched {} recruiter job applications with candidate preview for jobId: {}",
                applications.size(), jobId);

        return applications;
    }

    @Override
    public Boolean hasCandidateAppliedToJob(Long candidateId, Long jobId) {
        return jobApplicationRepository.existsByCandidateIdAndJobId(candidateId, jobId);
    }
}
