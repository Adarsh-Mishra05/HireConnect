package com.hireconnect.jobservice.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.hireconnect.jobservice.dto.request.JobRequestDto;
import com.hireconnect.jobservice.dto.response.JobResponseDto;
import com.hireconnect.jobservice.entity.ExperienceLevel;
import com.hireconnect.jobservice.entity.Job;
import com.hireconnect.jobservice.entity.JobStatus;
import com.hireconnect.jobservice.entity.JobType;

class JobMapperTest {

    private final JobMapper mapper = new JobMapper();

    @Test
    void toEntityAndUpdateEntity_MapsFields() {
        JobRequestDto request = request("Engineer", JobStatus.OPEN);
        Job job = mapper.toEntity(request, 99L);

        assertEquals("Engineer", job.getTitle());
        assertEquals(99L, job.getRecruiterId());
        assertEquals(JobStatus.OPEN, job.getStatus());

        JobRequestDto update = request("Senior Engineer", JobStatus.CLOSED);
        mapper.updateEntity(job, update);

        assertEquals("Senior Engineer", job.getTitle());
        assertEquals(JobStatus.CLOSED, job.getStatus());
    }

    @Test
    void toResponseDto_MapsAllResponseFields() {
        LocalDateTime now = LocalDateTime.now();
        Job job = Job.builder()
                .jobId(1L)
                .title("Backend Engineer")
                .description("Develop APIs")
                .companyName("HireConnect")
                .location("Bengaluru")
                .jobType(JobType.FULL_TIME)
                .experienceLevel(ExperienceLevel.SENIOR)
                .salaryMin(12.0)
                .salaryMax(24.0)
                .skillsRequired("Java,Spring")
                .status(JobStatus.OPEN)
                .recruiterId(7L)
                .createdAt(now)
                .updatedAt(now)
                .isFeatured(true)
                .build();

        JobResponseDto dto = mapper.toResponseDto(job);

        assertEquals(job.getJobId(), dto.getJobId());
        assertEquals(job.getTitle(), dto.getTitle());
        assertEquals(job.getRecruiterId(), dto.getRecruiterId());
        assertTrue(dto.getIsFeatured());
    }

    private JobRequestDto request(String title, JobStatus status) {
        JobRequestDto dto = new JobRequestDto();
        dto.setTitle(title);
        dto.setDescription("desc");
        dto.setCompanyName("HireConnect");
        dto.setLocation("Pune");
        dto.setJobType(JobType.FULL_TIME);
        dto.setExperienceLevel(ExperienceLevel.MID);
        dto.setSalaryMin(10.0);
        dto.setSalaryMax(20.0);
        dto.setSkillsRequired("Java");
        dto.setStatus(status);
        return dto;
    }
}

