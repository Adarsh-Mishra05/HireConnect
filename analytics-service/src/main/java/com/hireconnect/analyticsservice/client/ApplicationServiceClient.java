package com.hireconnect.analyticsservice.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hireconnect.analyticsservice.client.dto.ApplicationResponseDto;

@FeignClient(name = "application-service")
public interface ApplicationServiceClient {

    @GetMapping("/api/applications/internal/recruiter/{recruiterId}")
    List<ApplicationResponseDto> getApplicationsByRecruiter(@PathVariable("recruiterId") Long recruiterId);

    @GetMapping("/api/applications/internal/all")
    List<ApplicationResponseDto> getAllApplications();
}
