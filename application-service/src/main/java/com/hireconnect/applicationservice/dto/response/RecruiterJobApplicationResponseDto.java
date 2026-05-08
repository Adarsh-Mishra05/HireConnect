package com.hireconnect.applicationservice.dto.response;

import java.time.LocalDateTime;

import com.hireconnect.applicationservice.client.dto.CandidateProfilePreviewDto;
import com.hireconnect.applicationservice.enums.ApplicationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruiterJobApplicationResponseDto {
    private Long applicationId;
    private Long candidateId;
    private Long jobId;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private CandidateProfilePreviewDto candidateProfile;
}