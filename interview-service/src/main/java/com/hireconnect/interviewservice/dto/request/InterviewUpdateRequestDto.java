package com.hireconnect.interviewservice.dto.request;

import java.time.LocalDateTime;

import com.hireconnect.interviewservice.enums.InterviewStatus;
import com.hireconnect.interviewservice.enums.InterviewType;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterviewUpdateRequestDto {

    private InterviewType interviewType;

    @Future(message = "Interview must be scheduled for a future time")
    private LocalDateTime scheduledAt;

    @Min(value = 1, message = "Duration must be greater than 0")
    private Integer durationMinutes;

    private String meetingLink;
    private String location;
    private String notes;
    private InterviewStatus status;
}