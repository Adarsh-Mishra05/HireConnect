package com.hireconnect.interviewservice.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CandidateRescheduleRequestDto {

    @NotNull(message = "Requested date and time is required")
    @Future(message = "Requested schedule must be in the future")
    private LocalDateTime requestedScheduledAt;

    private String reason;
}
