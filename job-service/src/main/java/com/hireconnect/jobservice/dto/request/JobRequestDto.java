package com.hireconnect.jobservice.dto.request;

import com.hireconnect.jobservice.entity.ExperienceLevel;
import com.hireconnect.jobservice.entity.JobStatus;
import com.hireconnect.jobservice.entity.JobType;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    @NotNull(message = "Experience level is required")
    private ExperienceLevel experienceLevel;

    @NotNull(message = "Minimum salary is required")
    @DecimalMin(value = "0.0", message = "Minimum salary must be greater than or equal to 0")
    private Double salaryMin;

    @NotNull(message = "Maximum salary is required")
    @DecimalMin(value = "0.0", message = "Maximum salary must be greater than or equal to 0")
    private Double salaryMax;

    @NotBlank(message = "Skills required is required")
    private String skillsRequired;

    @NotNull(message = "Status is required")
    private JobStatus status;

    @AssertTrue(message = "Maximum salary must be greater than or equal to minimum salary")
    public boolean isSalaryRangeValid() {
        if (salaryMin == null || salaryMax == null) {
            return true;
        }
        return salaryMax >= salaryMin;
    }
}