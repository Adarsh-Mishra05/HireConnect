package com.hireconnect.applicationservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequestDto {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    private String resumeUrl;

    private String coverLetter;
}