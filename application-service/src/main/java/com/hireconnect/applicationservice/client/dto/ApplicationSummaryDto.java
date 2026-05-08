package com.hireconnect.applicationservice.client.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApplicationSummaryDto {

    private Long id;
    private Long jobId;
    private Long candidateId;
    private String candidateEmail;
    private Long recruiterId;
    private String status;
}