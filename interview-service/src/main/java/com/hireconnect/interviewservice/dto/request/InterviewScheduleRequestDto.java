package com.hireconnect.interviewservice.dto.request;

import java.time.LocalDateTime;

import com.hireconnect.interviewservice.enums.InterviewType;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterviewScheduleRequestDto {

    @NotNull(message = "Application id is required")
    private Long applicationId;

    @NotNull(message = "Interview type is required")
    private InterviewType interviewType;

    @NotNull(message = "Scheduled time is required")
    @Future(message = "Interview must be scheduled for a future time")
    private LocalDateTime scheduledAt;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be greater than 0")
    private Integer durationMinutes;

    private String meetingLink;
    private String location;
    private String notes;
}