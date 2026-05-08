package com.hireconnect.applicationservice.dto.request;

import com.hireconnect.applicationservice.enums.ApplicationStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationStatusUpdateRequestDto {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;
}