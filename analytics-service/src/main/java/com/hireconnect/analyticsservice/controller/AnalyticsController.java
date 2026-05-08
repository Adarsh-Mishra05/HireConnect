package com.hireconnect.analyticsservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.analyticsservice.dto.response.AdminAnalyticsResponseDto;
import com.hireconnect.analyticsservice.dto.response.RecruiterAnalyticsResponseDto;
import com.hireconnect.analyticsservice.enums.Role;
import com.hireconnect.analyticsservice.exception.UnauthorizedException;
import com.hireconnect.analyticsservice.security.AuthenticatedUser;
import com.hireconnect.analyticsservice.service.AnalyticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/recruiter/me")
    public ResponseEntity<RecruiterAnalyticsResponseDto> getMyRecruiterAnalytics(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        if (user == null || user.role() != Role.RECRUITER) {
            throw new UnauthorizedException("Only recruiters can access recruiter analytics");
        }

        return ResponseEntity.ok(analyticsService.getRecruiterAnalytics(user.userId()));
    }

    @GetMapping("/admin")
    public ResponseEntity<AdminAnalyticsResponseDto> getPlatformAnalytics(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        if (user == null || user.role() != Role.ADMIN) {
            throw new UnauthorizedException("Only admins can access platform analytics");
        }

        return ResponseEntity.ok(analyticsService.getPlatformAnalytics());
    }

    @GetMapping("/admin/export")
    public ResponseEntity<String> exportPlatformAnalyticsCsv(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        if (user == null || user.role() != Role.ADMIN) {
            throw new UnauthorizedException("Only admins can export platform analytics");
        }

        String csv = analyticsService.exportPlatformAnalyticsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=platform-analytics.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }
}
