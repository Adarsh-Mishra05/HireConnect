package com.hireconnect.analyticsservice.client.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationResponseDto {
    private Long id;
    private Long jobId;
    private Long candidateId;
    private Long recruiterId;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
