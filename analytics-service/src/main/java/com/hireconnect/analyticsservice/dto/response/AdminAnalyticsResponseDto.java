package com.hireconnect.analyticsservice.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminAnalyticsResponseDto {
    private long totalJobs;
    private long totalApplications;
    private long totalInterviews;
    private long shortlistedCount;
    private long offeredCount;
    private long rejectedCount;
    private long interviewsCompleted;
    private Double avgTimeToHireDays;
}
