package com.hireconnect.interviewservice.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hireconnect.interviewservice.client.ApplicationServiceClient;
import com.hireconnect.interviewservice.client.dto.ApplicationSummaryDto;
import com.hireconnect.interviewservice.dto.request.InterviewScheduleRequestDto;
import com.hireconnect.interviewservice.dto.request.InterviewUpdateRequestDto;
import com.hireconnect.interviewservice.dto.request.CandidateRescheduleRequestDto;
import com.hireconnect.interviewservice.dto.response.InterviewResponseDto;
import com.hireconnect.interviewservice.entity.Interview;
import com.hireconnect.interviewservice.enums.InterviewStatus;
import com.hireconnect.interviewservice.enums.NotificationType;
import com.hireconnect.interviewservice.enums.Role;
import com.hireconnect.interviewservice.event.NotificationEvent;
import com.hireconnect.interviewservice.exception.BadRequestException;
import com.hireconnect.interviewservice.exception.ResourceNotFoundException;
import com.hireconnect.interviewservice.exception.UnauthorizedException;
import com.hireconnect.interviewservice.producer.NotificationEventProducer;
import com.hireconnect.interviewservice.repository.InterviewRepository;
import com.hireconnect.interviewservice.security.AuthenticatedUser;
import com.hireconnect.interviewservice.service.InterviewService;

import lombok.RequiredArgsConstructor;

// [Adarsh Mishra] : Service implementation for interview scheduling and management.
// Handles scheduling, updates, and cancellations of interviews with candidate notifications.
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private static final Logger log = LoggerFactory.getLogger(InterviewServiceImpl.class);

    private final InterviewRepository interviewRepository;
    private final ApplicationServiceClient applicationServiceClient;
    private final NotificationEventProducer notificationEventProducer;

    // [Adarsh Mishra] : Schedules a new interview for a shortlisted job application.
    @Override
    @Transactional
    public InterviewResponseDto scheduleInterview(AuthenticatedUser user, InterviewScheduleRequestDto requestDto) {
        log.info("Interview scheduling started by recruiterId: {} for applicationId: {}",
                user.getUserId(), requestDto.getApplicationId());
        validateRecruiter(user.getRole());

        log.info("Fetching application summary for applicationId: {}", requestDto.getApplicationId());
        ApplicationSummaryDto application = applicationServiceClient.getApplicationSummary(requestDto.getApplicationId());

        if (application == null) {
            log.warn("Interview scheduling failed because application was not found for applicationId: {}",
                    requestDto.getApplicationId());
            throw new ResourceNotFoundException("Application not found");
        }

        if (!user.getUserId().equals(application.getRecruiterId())) {
            log.warn("Interview scheduling unauthorized for recruiterId: {} on applicationId: {} owned by recruiterId: {}",
                    user.getUserId(), application.getId(), application.getRecruiterId());
            throw new UnauthorizedException("You can schedule interviews only for your own job applications");
        }

        if (!"SHORTLISTED".equalsIgnoreCase(application.getStatus())) {
            log.warn("Interview scheduling failed because applicationId: {} has status: {}",
                    application.getId(), application.getStatus());
            throw new BadRequestException("Interview can only be scheduled for shortlisted applications");
        }

        Interview interview = Interview.builder()
                .applicationId(application.getId())
                .jobId(application.getJobId())
                .candidateId(application.getCandidateId())
                .candidateEmail(application.getCandidateEmail())
                .recruiterId(application.getRecruiterId())
                .interviewType(requestDto.getInterviewType())
                .scheduledAt(requestDto.getScheduledAt())
                .durationMinutes(requestDto.getDurationMinutes())
                .meetingLink(requestDto.getMeetingLink())
                .location(requestDto.getLocation())
                .notes(requestDto.getNotes())
                .status(InterviewStatus.SCHEDULED)
                .build();

        Interview savedInterview = interviewRepository.save(interview);
        log.info("Interview scheduled successfully with interviewId: {}, applicationId: {}, candidateId: {}",
                savedInterview.getId(), savedInterview.getApplicationId(), savedInterview.getCandidateId());

        sendInterviewScheduledNotification(savedInterview);
        return mapToResponse(savedInterview);
    }

    // [Adarsh Mishra] : Retrieves all interviews scheduled by the authenticated recruiter.
    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponseDto> getRecruiterInterviews(AuthenticatedUser user) {
        log.info("Fetching recruiter interviews for recruiterId: {}", user.getUserId());
        validateRecruiter(user.getRole());

        List<InterviewResponseDto> interviews = interviewRepository
                .findByRecruiterIdOrderByScheduledAtDesc(user.getUserId())
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Fetched {} interviews for recruiterId: {}", interviews.size(), user.getUserId());
        return interviews;
    }

    // [Adarsh Mishra] : Retrieves all interviews assigned to the authenticated candidate.
    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponseDto> getCandidateInterviews(AuthenticatedUser user) {
        log.info("Fetching candidate interviews for candidateId: {}", user.getUserId());
        validateCandidate(user.getRole());

        List<InterviewResponseDto> interviews = interviewRepository
                .findByCandidateIdOrderByScheduledAtDesc(user.getUserId())
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Fetched {} interviews for candidateId: {}", interviews.size(), user.getUserId());
        return interviews;
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewResponseDto getInterviewDetails(AuthenticatedUser user, Long interviewId) {
        log.info("Fetching interview details for interviewId: {}, userId: {}", interviewId, user.getUserId());

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> {
                    log.warn("Interview details fetch failed because interviewId: {} was not found", interviewId);
                    return new ResourceNotFoundException("Interview not found");
                });

        if (user.getRole() == Role.CANDIDATE && !interview.getCandidateId().equals(user.getUserId())) {
            log.warn("CandidateId: {} is not authorized to view interviewId: {}", user.getUserId(), interviewId);
            throw new UnauthorizedException("You can view only your interviews");
        }

        if (user.getRole() == Role.RECRUITER && !interview.getRecruiterId().equals(user.getUserId())) {
            log.warn("RecruiterId: {} is not authorized to view interviewId: {}", user.getUserId(), interviewId);
            throw new UnauthorizedException("You can view only your interviews");
        }

        log.info("Interview details fetched successfully for interviewId: {}", interviewId);
        return mapToResponse(interview);
    }

    // [Adarsh Mishra] : Updates existing interview details (time, link, notes) and status.
    @Override
    @Transactional
    public InterviewResponseDto updateInterview(AuthenticatedUser user, Long interviewId, InterviewUpdateRequestDto requestDto) {
        log.info("Interview update started for interviewId: {}, recruiterId: {}", interviewId, user.getUserId());
        validateRecruiter(user.getRole());

        Interview interview = interviewRepository.findByIdAndRecruiterId(interviewId, user.getUserId())
                .orElseThrow(() -> {
                    log.warn("Interview update failed because interviewId: {} was not found for recruiterId: {}",
                            interviewId, user.getUserId());
                    return new ResourceNotFoundException("Interview not found");
                });

        if (requestDto.getInterviewType() != null) interview.setInterviewType(requestDto.getInterviewType());
        if (requestDto.getScheduledAt() != null) interview.setScheduledAt(requestDto.getScheduledAt());
        if (requestDto.getDurationMinutes() != null) interview.setDurationMinutes(requestDto.getDurationMinutes());
        if (requestDto.getMeetingLink() != null) interview.setMeetingLink(requestDto.getMeetingLink());
        if (requestDto.getLocation() != null) interview.setLocation(requestDto.getLocation());
        if (requestDto.getNotes() != null) interview.setNotes(requestDto.getNotes());
        if (requestDto.getStatus() != null) interview.setStatus(requestDto.getStatus());

        Interview updatedInterview = interviewRepository.save(interview);
        log.info("Interview updated successfully for interviewId: {}, new status: {}",
                updatedInterview.getId(), updatedInterview.getStatus());

        sendInterviewUpdatedNotification(updatedInterview);
        return mapToResponse(updatedInterview);
    }

    // [Adarsh Mishra] : Cancels a scheduled interview and notifies the candidate.
    @Override
    @Transactional
    public InterviewResponseDto cancelInterview(AuthenticatedUser user, Long interviewId) {
        log.info("Interview cancellation started for interviewId: {}, recruiterId: {}", interviewId, user.getUserId());
        validateRecruiter(user.getRole());

        Interview interview = interviewRepository.findByIdAndRecruiterId(interviewId, user.getUserId())
                .orElseThrow(() -> {
                    log.warn("Interview cancellation failed because interviewId: {} was not found for recruiterId: {}",
                            interviewId, user.getUserId());
                    return new ResourceNotFoundException("Interview not found");
                });

        interview.setStatus(InterviewStatus.CANCELLED);
        Interview cancelledInterview = interviewRepository.save(interview);
        log.info("Interview cancelled successfully for interviewId: {}", cancelledInterview.getId());

        sendInterviewCancelledNotification(cancelledInterview);
        return mapToResponse(cancelledInterview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponseDto> getInterviewsByRecruiterIdInternal(Long recruiterId) {
        return interviewRepository.findByRecruiterIdOrderByScheduledAtDesc(recruiterId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponseDto> getAllInterviewsInternal() {
        return interviewRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public InterviewResponseDto confirmInterviewByCandidate(AuthenticatedUser user, Long interviewId) {
        validateCandidate(user.getRole());

        Interview interview = interviewRepository.findByIdAndCandidateId(interviewId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));

        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new BadRequestException("Cancelled interviews cannot be confirmed");
        }

        interview.setStatus(InterviewStatus.CONFIRMED);
        Interview updated = interviewRepository.save(interview);

        NotificationEvent event = NotificationEvent.builder()
                .recipientUserId(updated.getRecruiterId())
                .recipientEmail(null)
                .title("Interview Confirmed by Candidate")
                .message("Candidate #" + updated.getCandidateId() + " confirmed interview #" + updated.getId() + ".")
                .type(NotificationType.INTERVIEW)
                .sendEmail(false)
                .build();
        notificationEventProducer.sendNotification(event);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public InterviewResponseDto requestRescheduleByCandidate(
            AuthenticatedUser user,
            Long interviewId,
            CandidateRescheduleRequestDto requestDto
    ) {
        validateCandidate(user.getRole());

        Interview interview = interviewRepository.findByIdAndCandidateId(interviewId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));

        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new BadRequestException("Cancelled interviews cannot be rescheduled");
        }

        interview.setScheduledAt(requestDto.getRequestedScheduledAt());
        interview.setStatus(InterviewStatus.RESCHEDULED);

        String reason = requestDto.getReason() == null ? "" : requestDto.getReason().trim();
        String notePrefix = "Candidate requested reschedule.";
        if (!reason.isEmpty()) {
            notePrefix += " Reason: " + reason;
        }
        interview.setNotes(notePrefix + (interview.getNotes() != null ? " | " + interview.getNotes() : ""));

        Interview updated = interviewRepository.save(interview);

        NotificationEvent event = NotificationEvent.builder()
                .recipientUserId(updated.getRecruiterId())
                .recipientEmail(null)
                .title("Interview Reschedule Request")
                .message("Candidate #" + updated.getCandidateId() + " requested reschedule for interview #"
                        + updated.getId() + " to " + updated.getScheduledAt() + ".")
                .type(NotificationType.INTERVIEW)
                .sendEmail(false)
                .build();
        notificationEventProducer.sendNotification(event);

        return mapToResponse(updated);
    }

    private void validateRecruiter(Role role) {
        if (role != Role.RECRUITER) {
            log.warn("Recruiter-only action attempted by role: {}", role);
            throw new UnauthorizedException("Only recruiters can perform this action");
        }
    }

    private void validateCandidate(Role role) {
        if (role != Role.CANDIDATE) {
            log.warn("Candidate-only action attempted by role: {}", role);
            throw new UnauthorizedException("Only candidates can perform this action");
        }
    }

    private InterviewResponseDto mapToResponse(Interview interview) {
        return InterviewResponseDto.builder()
                .id(interview.getId())
                .applicationId(interview.getApplicationId())
                .jobId(interview.getJobId())
                .candidateId(interview.getCandidateId())
                .recruiterId(interview.getRecruiterId())
                .interviewType(interview.getInterviewType())
                .scheduledAt(interview.getScheduledAt())
                .durationMinutes(interview.getDurationMinutes())
                .meetingLink(interview.getMeetingLink())
                .location(interview.getLocation())
                .notes(interview.getNotes())
                .status(interview.getStatus())
                .createdAt(interview.getCreatedAt())
                .updatedAt(interview.getUpdatedAt())
                .build();
    }

    private void sendInterviewScheduledNotification(Interview interview) {
        log.info("Sending scheduled interview notification for interviewId: {}, candidateId: {}",
                interview.getId(), interview.getCandidateId());

        String message =
                "Dear Candidate,\n\n"
                + "Greetings from HireConnect.\n\n"
                + "We are pleased to inform you that your interview has been scheduled.\n\n"
                + "Interview Details:\n"
                + "Job ID: " + interview.getJobId() + "\n"
                + "Date & Time: " + interview.getScheduledAt() + "\n"
                + "Interview Mode: " + interview.getInterviewType().name() + "\n"
                + (interview.getMeetingLink() != null
                    ? "Meeting Link: " + interview.getMeetingLink() + "\n"
                    : "")
                + (interview.getLocation() != null
                    ? "Location: " + interview.getLocation() + "\n"
                    : "")
                + "\nPlease ensure you are available on time and prepared for the interview.\n\n"
                + "We wish you all the best.\n\n"
                + "Regards,\n"
                + "Recruitment Team\n"
                + "HireConnect";

        NotificationEvent event = NotificationEvent.builder()
                .recipientUserId(interview.getCandidateId())
                .recipientEmail(interview.getCandidateEmail())
                .title("Interview Scheduled")
                .message(message)
                .type(NotificationType.INTERVIEW)
                .sendEmail(true)
                .build();

        notificationEventProducer.sendNotification(event);
        log.info("Scheduled interview notification event published for interviewId: {}", interview.getId());
    }

    private void sendInterviewUpdatedNotification(Interview interview) {
        log.info("Sending updated interview notification for interviewId: {}, candidateId: {}",
                interview.getId(), interview.getCandidateId());

        String message =
                "Dear Candidate,\n\n"
                + "Greetings from HireConnect.\n\n"
                + "Your interview details have been updated.\n\n"
                + "Updated Information:\n"
                + "Job ID: " + interview.getJobId() + "\n"
                + "Current Status: " + interview.getStatus().name() + "\n"
                + "Date & Time: " + interview.getScheduledAt() + "\n"
                + "Interview Mode: " + interview.getInterviewType().name() + "\n"
                + (interview.getMeetingLink() != null
                    ? "Meeting Link: " + interview.getMeetingLink() + "\n"
                    : "")
                + (interview.getLocation() != null
                    ? "Location: " + interview.getLocation() + "\n"
                    : "")
                + "\nPlease review the updated details carefully.\n\n"
                + "We wish you the very best.\n\n"
                + "Regards,\n"
                + "Recruitment Team\n"
                + "HireConnect";

        NotificationEvent event = NotificationEvent.builder()
                .recipientUserId(interview.getCandidateId())
                .recipientEmail(interview.getCandidateEmail())
                .title("Interview Updated")
                .message(message)
                .type(NotificationType.INTERVIEW)
                .sendEmail(true)
                .build();

        notificationEventProducer.sendNotification(event);
        log.info("Updated interview notification event published for interviewId: {}", interview.getId());
    }

    private void sendInterviewCancelledNotification(Interview interview) {
        log.info("Sending cancelled interview notification for interviewId: {}, candidateId: {}",
                interview.getId(), interview.getCandidateId());

        String message =
                "Dear Candidate,\n\n"
                + "Greetings from HireConnect.\n\n"
                + "We regret to inform you that your interview has been cancelled.\n\n"
                + "Details:\n"
                + "Job ID: " + interview.getJobId() + "\n"
                + "Status: CANCELLED\n\n"
                + "If required, the recruiter will reach out to you regarding further steps.\n\n"
                + "Thank you for your understanding.\n\n"
                + "Regards,\n"
                + "Recruitment Team\n"
                + "HireConnect";

        NotificationEvent event = NotificationEvent.builder()
                .recipientUserId(interview.getCandidateId())
                .recipientEmail(interview.getCandidateEmail())
                .title("Interview Cancelled")
                .message(message)
                .type(NotificationType.INTERVIEW)
                .sendEmail(true)
                .build();

        notificationEventProducer.sendNotification(event);
        log.info("Cancelled interview notification event published for interviewId: {}", interview.getId());
    }
}
