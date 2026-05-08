package com.hireconnect.analyticsservice.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hireconnect.analyticsservice.client.ApplicationServiceClient;
import com.hireconnect.analyticsservice.client.InterviewServiceClient;
import com.hireconnect.analyticsservice.client.JobServiceClient;
import com.hireconnect.analyticsservice.client.dto.ApplicationResponseDto;
import com.hireconnect.analyticsservice.client.dto.ApplicationStatus;
import com.hireconnect.analyticsservice.client.dto.InterviewResponseDto;
import com.hireconnect.analyticsservice.client.dto.InterviewStatus;
import com.hireconnect.analyticsservice.dto.response.AdminAnalyticsResponseDto;
import com.hireconnect.analyticsservice.dto.response.RecruiterAnalyticsResponseDto;
import com.hireconnect.analyticsservice.service.AnalyticsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final JobServiceClient jobServiceClient;
    private final ApplicationServiceClient applicationServiceClient;
    private final InterviewServiceClient interviewServiceClient;

    @Override
    public RecruiterAnalyticsResponseDto getRecruiterAnalytics(Long recruiterId) {
        List<Long> recruiterJobIds = safeList(jobServiceClient.getJobIdsByRecruiter(recruiterId));
        List<ApplicationResponseDto> applications = safeList(applicationServiceClient.getApplicationsByRecruiter(recruiterId));
        List<InterviewResponseDto> interviews = safeList(interviewServiceClient.getInterviewsByRecruiter(recruiterId));

        long totalJobs = recruiterJobIds.size();
        long totalApplications = applications.size();
        long shortlisted = countApplications(applications, ApplicationStatus.SHORTLISTED);
        long offered = countApplications(applications, ApplicationStatus.ACCEPTED);
        long rejected = countApplications(applications, ApplicationStatus.REJECTED);

        long interviewsScheduled = interviews.stream()
                .filter(i -> i.getStatus() == InterviewStatus.SCHEDULED || i.getStatus() == InterviewStatus.RESCHEDULED)
                .count();

        long interviewsCompleted = interviews.stream()
                .filter(i -> i.getStatus() == InterviewStatus.COMPLETED)
                .count();

        double avgTimeToHire = computeAvgTimeToHireDays(applications, interviews);

        // Approximation with currently available data.
        double viewToApplyRatio = totalApplications == 0 ? 0.0 : ((double) totalApplications / Math.max(totalJobs, 1));

        return RecruiterAnalyticsResponseDto.builder()
                .recruiterId(recruiterId)
                .totalJobs(totalJobs)
                .totalApplications(totalApplications)
                .shortlistedCount(shortlisted)
                .offeredCount(offered)
                .rejectedCount(rejected)
                .interviewsScheduled(interviewsScheduled)
                .interviewsCompleted(interviewsCompleted)
                .avgTimeToHireDays(round2(avgTimeToHire))
                .viewToApplyRatio(round2(viewToApplyRatio))
                .build();
    }

    @Override
    public AdminAnalyticsResponseDto getPlatformAnalytics() {
        long totalJobs = safeLong(jobServiceClient.countAllJobs());
        List<ApplicationResponseDto> applications = safeList(applicationServiceClient.getAllApplications());
        List<InterviewResponseDto> interviews = safeList(interviewServiceClient.getAllInterviews());

        long shortlisted = countApplications(applications, ApplicationStatus.SHORTLISTED);
        long offered = countApplications(applications, ApplicationStatus.ACCEPTED);
        long rejected = countApplications(applications, ApplicationStatus.REJECTED);
        long completedInterviews = interviews.stream().filter(i -> i.getStatus() == InterviewStatus.COMPLETED).count();

        double avgTimeToHire = computeAvgTimeToHireDays(applications, interviews);

        return AdminAnalyticsResponseDto.builder()
                .totalJobs(totalJobs)
                .totalApplications(applications.size())
                .totalInterviews(interviews.size())
                .shortlistedCount(shortlisted)
                .offeredCount(offered)
                .rejectedCount(rejected)
                .interviewsCompleted(completedInterviews)
                .avgTimeToHireDays(round2(avgTimeToHire))
                .build();
    }

    @Override
    public String exportPlatformAnalyticsCsv() {
        AdminAnalyticsResponseDto stats = getPlatformAnalytics();
        return "metric,value\n"
                + "totalJobs," + stats.getTotalJobs() + "\n"
                + "totalApplications," + stats.getTotalApplications() + "\n"
                + "totalInterviews," + stats.getTotalInterviews() + "\n"
                + "shortlistedCount," + stats.getShortlistedCount() + "\n"
                + "offeredCount," + stats.getOfferedCount() + "\n"
                + "rejectedCount," + stats.getRejectedCount() + "\n"
                + "interviewsCompleted," + stats.getInterviewsCompleted() + "\n"
                + "avgTimeToHireDays," + stats.getAvgTimeToHireDays() + "\n";
    }

    private long countApplications(List<ApplicationResponseDto> applications, ApplicationStatus status) {
        return applications.stream().filter(a -> a.getStatus() == status).count();
    }

    private double computeAvgTimeToHireDays(List<ApplicationResponseDto> applications, List<InterviewResponseDto> interviews) {
        Map<Long, ApplicationResponseDto> applicationById = applications.stream()
                .filter(a -> a.getId() != null)
                .collect(Collectors.toMap(ApplicationResponseDto::getId, Function.identity(), (a, b) -> a));

        List<InterviewResponseDto> completed = interviews.stream()
                .filter(i -> i.getStatus() == InterviewStatus.COMPLETED)
                .filter(i -> i.getApplicationId() != null)
                .toList();

        if (completed.isEmpty()) {
            return 0.0;
        }

        double avgHours = completed.stream()
                .map(i -> {
                    ApplicationResponseDto app = applicationById.get(i.getApplicationId());
                    LocalDateTime started = app != null ? app.getAppliedAt() : null;
                    LocalDateTime ended = i.getUpdatedAt() != null ? i.getUpdatedAt() : i.getCreatedAt();
                    if (started == null || ended == null || ended.isBefore(started)) {
                        return 0.0;
                    }
                    return (double) Duration.between(started, ended).toHours();
                })
                .sorted(Comparator.naturalOrder())
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        return avgHours / 24.0;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private <T> List<T> safeList(List<T> input) {
        return input == null ? List.of() : input;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }
}
