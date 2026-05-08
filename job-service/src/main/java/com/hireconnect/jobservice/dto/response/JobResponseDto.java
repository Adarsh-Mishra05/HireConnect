package com.hireconnect.jobservice.dto.response;

import java.time.LocalDateTime;

import com.hireconnect.jobservice.entity.ExperienceLevel;
import com.hireconnect.jobservice.entity.JobStatus;
import com.hireconnect.jobservice.entity.JobType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobResponseDto {

    private Long jobId;
    private String title;
    private String description;
    private String companyName;
    private String location;
    private JobType jobType;
    private ExperienceLevel experienceLevel;
    private Double salaryMin;
    private Double salaryMax;
    private String skillsRequired;
    private JobStatus status;
    private Long recruiterId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isFeatured;	
}