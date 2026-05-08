package com.hireconnect.applicationservice.dto.response;

import java.time.LocalDateTime;

import com.hireconnect.applicationservice.enums.ApplicationStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApplicationResponseDto {

    private Long id;
    private Long jobId;
    private Long candidateId;
    private Long recruiterId;

    private ApplicationStatus status;

    private String resumeUrl;
    private String coverLetter;

    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}