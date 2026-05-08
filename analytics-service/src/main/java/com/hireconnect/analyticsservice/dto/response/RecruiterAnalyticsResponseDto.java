package com.hireconnect.analyticsservice.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecruiterAnalyticsResponseDto {
    private Long recruiterId;
    private long totalJobs;
    private long totalApplications;
    private long shortlistedCount;
    private long offeredCount;
    private long rejectedCount;
    private long interviewsScheduled;
    private long interviewsCompleted;
    private Double avgTimeToHireDays;
    private Double viewToApplyRatio;
}
