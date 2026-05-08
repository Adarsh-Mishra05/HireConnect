package com.hireconnect.analyticsservice.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hireconnect.analyticsservice.client.dto.InterviewResponseDto;

@FeignClient(name = "interview-service")
public interface InterviewServiceClient {

    @GetMapping("/api/interviews/internal/recruiter/{recruiterId}")
    List<InterviewResponseDto> getInterviewsByRecruiter(@PathVariable("recruiterId") Long recruiterId);

    @GetMapping("/api/interviews/internal/all")
    List<InterviewResponseDto> getAllInterviews();
}
