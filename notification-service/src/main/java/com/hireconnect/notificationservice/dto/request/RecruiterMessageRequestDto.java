package com.hireconnect.notificationservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecruiterMessageRequestDto {

    @NotNull(message = "Candidate userId is required")
    private Long candidateId;

    @NotNull(message = "Job id is required")
    private Long jobId;

    @NotBlank(message = "Message is required")
    private String message;
}
