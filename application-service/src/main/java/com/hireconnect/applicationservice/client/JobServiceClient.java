package com.hireconnect.applicationservice.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hireconnect.applicationservice.config.FeignClientConfig;

@FeignClient(name = "job-service", configuration = FeignClientConfig.class)
public interface JobServiceClient {

    @GetMapping("/api/jobs/internal/{jobId}/exists")
    Boolean doesJobExist(@PathVariable("jobId") Long jobId);

    @GetMapping("/api/jobs/internal/{jobId}/open")
    Boolean isJobOpen(@PathVariable("jobId") Long jobId);

    @GetMapping("/api/jobs/internal/{jobId}/recruiter/{recruiterId}/ownership")
    Boolean isJobOwnedByRecruiter(@PathVariable("jobId") Long jobId,
                                  @PathVariable("recruiterId") Long recruiterId);

    @GetMapping("/api/jobs/internal/recruiter/{recruiterId}/job-ids")
    List<Long> getJobIdsByRecruiter(@PathVariable("recruiterId") Long recruiterId);

    @GetMapping("/api/jobs/internal/{jobId}/recruiter-id")
    Long getRecruiterIdByJobId(@PathVariable("jobId") Long jobId);
}