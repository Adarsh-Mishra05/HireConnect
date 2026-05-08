package com.hireconnect.analyticsservice.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "job-service")
public interface JobServiceClient {

    @GetMapping("/api/jobs/internal/recruiter/{recruiterId}/job-ids")
    List<Long> getJobIdsByRecruiter(@PathVariable("recruiterId") Long recruiterId);

    @GetMapping("/api/jobs/internal/count")
    Long countAllJobs();
}
