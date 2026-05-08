package com.hireconnect.analyticsservice.service;

import com.hireconnect.analyticsservice.dto.response.AdminAnalyticsResponseDto;
import com.hireconnect.analyticsservice.dto.response.RecruiterAnalyticsResponseDto;

public interface AnalyticsService {

    RecruiterAnalyticsResponseDto getRecruiterAnalytics(Long recruiterId);

    AdminAnalyticsResponseDto getPlatformAnalytics();

    String exportPlatformAnalyticsCsv();
}
