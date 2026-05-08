package com.hireconnect.analyticsservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hireconnect.analyticsservice.client.ApplicationServiceClient;
import com.hireconnect.analyticsservice.client.InterviewServiceClient;
import com.hireconnect.analyticsservice.client.JobServiceClient;
import com.hireconnect.analyticsservice.client.dto.ApplicationResponseDto;
import com.hireconnect.analyticsservice.client.dto.ApplicationStatus;
import com.hireconnect.analyticsservice.client.dto.InterviewResponseDto;
import com.hireconnect.analyticsservice.client.dto.InterviewStatus;
import com.hireconnect.analyticsservice.dto.response.AdminAnalyticsResponseDto;
import com.hireconnect.analyticsservice.dto.response.RecruiterAnalyticsResponseDto;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private JobServiceClient jobServiceClient;

    @Mock
    private ApplicationServiceClient applicationServiceClient;

    @Mock
    private InterviewServiceClient interviewServiceClient;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private Long recruiterId = 1L;
    private List<Long> jobIds;
    private List<ApplicationResponseDto> applications;
    private List<InterviewResponseDto> interviews;

    @BeforeEach
    void setUp() {
        jobIds = List.of(101L, 102L);
        
        ApplicationResponseDto app1 = new ApplicationResponseDto();
        app1.setId(1L);
        app1.setStatus(ApplicationStatus.ACCEPTED);
        app1.setAppliedAt(LocalDateTime.now().minusDays(5));

        ApplicationResponseDto app2 = new ApplicationResponseDto();
        app2.setId(2L);
        app2.setStatus(ApplicationStatus.SHORTLISTED);
        app2.setAppliedAt(LocalDateTime.now().minusDays(3));

        applications = List.of(app1, app2);

        InterviewResponseDto int1 = new InterviewResponseDto();
        int1.setId(1L);
        int1.setApplicationId(1L);
        int1.setStatus(InterviewStatus.COMPLETED);
        int1.setUpdatedAt(LocalDateTime.now());

        interviews = List.of(int1);
    }

    @Test
    void getRecruiterAnalytics_ShouldReturnCorrectStats() {
        when(jobServiceClient.getJobIdsByRecruiter(recruiterId)).thenReturn(jobIds);
        when(applicationServiceClient.getApplicationsByRecruiter(recruiterId)).thenReturn(applications);
        when(interviewServiceClient.getInterviewsByRecruiter(recruiterId)).thenReturn(interviews);

        RecruiterAnalyticsResponseDto result = analyticsService.getRecruiterAnalytics(recruiterId);

        assertNotNull(result);
        assertEquals(recruiterId, result.getRecruiterId());
        assertEquals(2, result.getTotalJobs());
        assertEquals(2, result.getTotalApplications());
        assertEquals(1, result.getOfferedCount());
        assertEquals(1, result.getShortlistedCount());
        assertEquals(1, result.getInterviewsCompleted());
        assertTrue(result.getAvgTimeToHireDays() > 0);
    }

    @Test
    void getPlatformAnalytics_ShouldReturnCorrectStats() {
        when(jobServiceClient.countAllJobs()).thenReturn(100L);
        when(applicationServiceClient.getAllApplications()).thenReturn(applications);
        when(interviewServiceClient.getAllInterviews()).thenReturn(interviews);

        AdminAnalyticsResponseDto result = analyticsService.getPlatformAnalytics();

        assertNotNull(result);
        assertEquals(100, result.getTotalJobs());
        assertEquals(2, result.getTotalApplications());
        assertEquals(1, result.getOfferedCount());
    }

    @Test
    void exportPlatformAnalyticsCsv_ShouldReturnCsvString() {
        when(jobServiceClient.countAllJobs()).thenReturn(100L);
        when(applicationServiceClient.getAllApplications()).thenReturn(applications);
        when(interviewServiceClient.getAllInterviews()).thenReturn(interviews);

        String csv = analyticsService.exportPlatformAnalyticsCsv();

        assertNotNull(csv);
        assertTrue(csv.contains("totalJobs,100"));
        assertTrue(csv.contains("totalApplications,2"));
    }

    @Test
    void getRecruiterAnalytics_WithEmptyData_ShouldReturnZeroStats() {
        when(jobServiceClient.getJobIdsByRecruiter(recruiterId)).thenReturn(null);
        when(applicationServiceClient.getApplicationsByRecruiter(recruiterId)).thenReturn(List.of());
        when(interviewServiceClient.getInterviewsByRecruiter(recruiterId)).thenReturn(null);

        RecruiterAnalyticsResponseDto result = analyticsService.getRecruiterAnalytics(recruiterId);

        assertNotNull(result);
        assertEquals(0, result.getTotalJobs());
        assertEquals(0, result.getTotalApplications());
        assertEquals(0.0, result.getAvgTimeToHireDays());
    }
}
