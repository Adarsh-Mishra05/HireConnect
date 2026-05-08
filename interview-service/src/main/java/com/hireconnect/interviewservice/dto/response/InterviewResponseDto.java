package com.hireconnect.interviewservice.dto.response;

import java.time.LocalDateTime;

import com.hireconnect.interviewservice.enums.InterviewStatus;
import com.hireconnect.interviewservice.enums.InterviewType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class InterviewResponseDto {

    private Long id;
    private Long applicationId;
    private Long jobId;
    private Long candidateId;
    private Long recruiterId;
    private InterviewType interviewType;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String meetingLink;
    private String location;
    private String notes;
    private InterviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}