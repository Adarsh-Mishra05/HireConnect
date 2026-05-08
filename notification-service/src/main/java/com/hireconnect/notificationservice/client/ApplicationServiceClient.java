package com.hireconnect.notificationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "application-service")
public interface ApplicationServiceClient {

    @GetMapping("/api/applications/internal/shortlisted-check")
    Boolean isCandidateShortlistedForRecruiterJob(
            @RequestParam("recruiterId") Long recruiterId,
            @RequestParam("candidateId") Long candidateId,
            @RequestParam("jobId") Long jobId
    );
}
