package com.hireconnect.interviewservice.service;

import java.util.List;

import com.hireconnect.interviewservice.dto.request.InterviewScheduleRequestDto;
import com.hireconnect.interviewservice.dto.request.InterviewUpdateRequestDto;
import com.hireconnect.interviewservice.dto.request.CandidateRescheduleRequestDto;
import com.hireconnect.interviewservice.dto.response.InterviewResponseDto;
import com.hireconnect.interviewservice.security.AuthenticatedUser;

// [Disha Gujar] : Service interface defining the business logic contract for interview management.
// Covers interview scheduling by recruiters, retrieval for both recruiter and candidate views,
// detail lookup by interview ID, interview detail updates, and interview cancellation workflows.
public interface InterviewService {

    InterviewResponseDto scheduleInterview(AuthenticatedUser user, InterviewScheduleRequestDto requestDto);

    List<InterviewResponseDto> getRecruiterInterviews(AuthenticatedUser user);

    List<InterviewResponseDto> getCandidateInterviews(AuthenticatedUser user);

    InterviewResponseDto getInterviewDetails(AuthenticatedUser user, Long interviewId);

    InterviewResponseDto updateInterview(AuthenticatedUser user, Long interviewId, InterviewUpdateRequestDto requestDto);

    InterviewResponseDto cancelInterview(AuthenticatedUser user, Long interviewId);

    List<InterviewResponseDto> getInterviewsByRecruiterIdInternal(Long recruiterId);

    List<InterviewResponseDto> getAllInterviewsInternal();

    InterviewResponseDto confirmInterviewByCandidate(AuthenticatedUser user, Long interviewId);

    InterviewResponseDto requestRescheduleByCandidate(
            AuthenticatedUser user,
            Long interviewId,
            CandidateRescheduleRequestDto requestDto
    );
}
