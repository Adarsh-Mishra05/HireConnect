package com.hireconnect.jobservice.service;

import java.util.List;

import com.hireconnect.jobservice.dto.request.JobRequestDto;
import com.hireconnect.jobservice.dto.response.JobResponseDto;
import com.hireconnect.jobservice.entity.ExperienceLevel;
import com.hireconnect.jobservice.entity.JobStatus;
import com.hireconnect.jobservice.entity.JobType;
import com.hireconnect.jobservice.entity.Role;

public interface JobService {

    JobResponseDto createJob(Long userId, Role role, JobRequestDto requestDto);

    JobResponseDto updateJob(Long jobId, Long userId, Role role, JobRequestDto requestDto);

    void deleteJob(Long jobId, Long userId, Role role);

    List<JobResponseDto> getMyJobs(Long userId, Role role);

    List<JobResponseDto> getAllOpenJobs();

    List<JobResponseDto> getAllJobsForAdmin(Long userId, Role role);

    JobResponseDto getOpenJobById(Long jobId);

    List<JobResponseDto> searchOpenJobs(
            String keyword,
            String location,
            JobType jobType,
            ExperienceLevel experienceLevel,
            Double minSalary,
            Double maxSalary
    );

    boolean doesJobExist(Long jobId);

    boolean isJobOpen(Long jobId);

    boolean isJobOwnedByRecruiter(Long jobId, Long recruiterId);

    List<Long> getJobIdsByRecruiter(Long recruiterId);

    Long getRecruiterIdByJobId(Long jobId);
    
    void markAsFeatured(Long jobId, Long recruiterId, Role role);

    long countAllJobs();

    void saveJobForCandidate(Long jobId, Long userId, Role role);

    void removeSavedJobForCandidate(Long jobId, Long userId, Role role);

    List<JobResponseDto> getSavedJobsForCandidate(Long userId, Role role);

    JobResponseDto updateJobStatusByAdmin(Long adminUserId, Role role, Long jobId, JobStatus status);

    void deleteJobByAdmin(Long adminUserId, Role role, Long jobId);
}
