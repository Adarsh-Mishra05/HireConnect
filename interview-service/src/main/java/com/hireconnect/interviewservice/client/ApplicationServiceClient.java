package com.hireconnect.interviewservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hireconnect.interviewservice.client.dto.ApplicationSummaryDto;
import com.hireconnect.interviewservice.config.FeignClientConfig;

@FeignClient(name = "application-service", configuration = FeignClientConfig.class)
public interface ApplicationServiceClient {

    @GetMapping("/api/applications/internal/{applicationId}")
    ApplicationSummaryDto getApplicationSummary(@PathVariable("applicationId") Long applicationId);
}