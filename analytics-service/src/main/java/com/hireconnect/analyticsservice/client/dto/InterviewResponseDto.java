package com.hireconnect.analyticsservice.client.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterviewResponseDto {
    private Long id;
    private Long applicationId;
    private Long jobId;
    private Long candidateId;
    private Long recruiterId;
    private InterviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
