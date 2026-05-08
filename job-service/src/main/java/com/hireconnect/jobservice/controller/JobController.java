package com.hireconnect.jobservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hireconnect.jobservice.dto.request.JobRequestDto;
import com.hireconnect.jobservice.dto.response.JobResponseDto;
import com.hireconnect.jobservice.entity.ExperienceLevel;
import com.hireconnect.jobservice.entity.JobStatus;
import com.hireconnect.jobservice.entity.JobType;
import com.hireconnect.jobservice.security.AuthenticatedUser;
import com.hireconnect.jobservice.service.JobService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponseDto> createJob(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody JobRequestDto requestDto
    ) {
        log.info("Create job request received for recruiterId={} with title={}", user.getUserId(), requestDto.getTitle());
        JobResponseDto response = jobService.createJob(
                user.getUserId(),
                user.getRole(),
                requestDto
        );
        log.info("Job created successfully with jobId={} for recruiterId={}", response.getJobId(), user.getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<JobResponseDto> updateJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody JobRequestDto requestDto
    ) {
        log.info("Update job request received for jobId={} by recruiterId={}", jobId, user.getUserId());
        JobResponseDto response = jobService.updateJob(
                jobId,
                user.getUserId(),
                user.getRole(),
                requestDto
        );
        log.info("Job updated successfully for jobId={} by recruiterId={}", jobId, user.getUserId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<String> deleteJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Delete job request received for jobId={} by recruiterId={}", jobId, user.getUserId());
        jobService.deleteJob(jobId, user.getUserId(), user.getRole());
        log.info("Job deleted successfully for jobId={} by recruiterId={}", jobId, user.getUserId());
        return ResponseEntity.ok("Job deleted successfully");
    }

    @GetMapping("/recruiter/me")
    public ResponseEntity<List<JobResponseDto>> getMyJobs(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Fetch recruiter jobs request received for recruiterId={}", user.getUserId());
        return ResponseEntity.ok(jobService.getMyJobs(user.getUserId(), user.getRole()));
    }

    @GetMapping
    public ResponseEntity<List<JobResponseDto>> getAllOpenJobs() {
        log.info("Fetch all open jobs request received");
        return ResponseEntity.ok(jobService.getAllOpenJobs());
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<JobResponseDto>> getAllJobsForAdmin(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Fetch all jobs (admin) request received for adminId={}", user.getUserId());
        return ResponseEntity.ok(jobService.getAllJobsForAdmin(user.getUserId(), user.getRole()));
    }

    @PatchMapping("/admin/{jobId}/status")
    public ResponseEntity<JobResponseDto> updateJobStatusByAdmin(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long jobId,
            @RequestParam JobStatus status
    ) {
        log.info("Admin status update request for jobId={} by adminId={} to status={}",
                jobId, user.getUserId(), status);
        return ResponseEntity.ok(jobService.updateJobStatusByAdmin(user.getUserId(), user.getRole(), jobId, status));
    }

    @DeleteMapping("/admin/{jobId}")
    public ResponseEntity<String> deleteJobByAdmin(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long jobId
    ) {
        log.info("Admin delete job request for jobId={} by adminId={}", jobId, user.getUserId());
        jobService.deleteJobByAdmin(user.getUserId(), user.getRole(), jobId);
        return ResponseEntity.ok("Job deleted successfully");
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponseDto> getOpenJobById(@PathVariable Long jobId) {
        log.info("Fetch open job by id request received for jobId={}", jobId);
        return ResponseEntity.ok(jobService.getOpenJobById(jobId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<JobResponseDto>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary
    ) {
        log.info("Search jobs request received with keyword={}, location={}, jobType={}, experienceLevel={}, minSalary={}, maxSalary={}", keyword, location, jobType, experienceLevel, minSalary, maxSalary);
        return ResponseEntity.ok(
                jobService.searchOpenJobs(
                        keyword,
                        location,
                        jobType,
                        experienceLevel,
                        minSalary,
                        maxSalary
                )
        );
    }

    @GetMapping("/internal/{jobId}/exists")
    public ResponseEntity<Boolean> doesJobExist(@PathVariable Long jobId) {
        log.info("Internal exists check request received for jobId={}", jobId);
        return ResponseEntity.ok(jobService.doesJobExist(jobId));
    }

    @GetMapping("/internal/{jobId}/open")
    public ResponseEntity<Boolean> isJobOpen(@PathVariable Long jobId) {
        log.info("Internal open status check request received for jobId={}", jobId);
        return ResponseEntity.ok(jobService.isJobOpen(jobId));
    }

    @GetMapping("/internal/{jobId}/recruiter/{recruiterId}/ownership")
    public ResponseEntity<Boolean> isJobOwnedByRecruiter(
            @PathVariable Long jobId,
            @PathVariable Long recruiterId
    ) {
        log.info("Internal ownership check request received for jobId={} and recruiterId={}", jobId, recruiterId);
        return ResponseEntity.ok(jobService.isJobOwnedByRecruiter(jobId, recruiterId));
    }

    @GetMapping("/internal/recruiter/{recruiterId}/job-ids")
    public ResponseEntity<List<Long>> getJobIdsByRecruiter(@PathVariable Long recruiterId) {
        log.info("Internal recruiter job ids request received for recruiterId={}", recruiterId);
        return ResponseEntity.ok(jobService.getJobIdsByRecruiter(recruiterId));
    }

    @GetMapping("/internal/{jobId}/recruiter-id")
    public ResponseEntity<Long> getRecruiterIdByJobId(@PathVariable Long jobId) {
        log.info("Internal recruiter id lookup request received for jobId={}", jobId);
        return ResponseEntity.ok(jobService.getRecruiterIdByJobId(jobId));
    }

    @GetMapping("/internal/count")
    public ResponseEntity<Long> countAllJobsInternal() {
        log.info("Internal job count request received");
        return ResponseEntity.ok(jobService.countAllJobs());
    }
    
    @PutMapping("/{jobId}/feature")
    public ResponseEntity<String> markJobAsFeatured(
            @PathVariable Long jobId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Feature job request received for jobId={} by recruiterId={}", jobId, user != null ? user.getUserId() : null);
        jobService.markAsFeatured(jobId, user.getUserId(), user.getRole());
        log.info("Job marked as featured successfully for jobId={}", jobId);
        return ResponseEntity.ok("Job marked as featured");
    }

    @PostMapping("/{jobId}/bookmark")
    public ResponseEntity<String> saveJobForCandidate(
            @PathVariable Long jobId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        jobService.saveJobForCandidate(jobId, user.getUserId(), user.getRole());
        return ResponseEntity.ok("Job saved");
    }

    @DeleteMapping("/{jobId}/bookmark")
    public ResponseEntity<String> removeSavedJobForCandidate(
            @PathVariable Long jobId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        jobService.removeSavedJobForCandidate(jobId, user.getUserId(), user.getRole());
        return ResponseEntity.ok("Saved job removed");
    }

    @GetMapping("/bookmarks/me")
    public ResponseEntity<List<JobResponseDto>> getSavedJobsForCandidate(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(jobService.getSavedJobsForCandidate(user.getUserId(), user.getRole()));
    }
}
