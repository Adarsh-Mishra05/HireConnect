package com.hireconnect.interviewservice.client.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationSummaryDto {
    private Long id;
    private Long jobId;
    private Long candidateId;
    private String candidateEmail;
    private Long recruiterId;
    private String status;
}