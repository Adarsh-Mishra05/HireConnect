package com.hireconnect.profileservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "job-service")
public interface JobServiceClient {

    @GetMapping("/api/jobs/internal/{jobId}/recruiter/{recruiterId}/ownership")
    Boolean isJobOwnedByRecruiter(
            @PathVariable Long jobId,
            @PathVariable Long recruiterId
    );
}