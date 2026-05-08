package com.hireconnect.jobservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.hireconnect.jobservice.dto.request.JobRequestDto;
import com.hireconnect.jobservice.dto.response.JobResponseDto;
import com.hireconnect.jobservice.entity.Job;
import com.hireconnect.jobservice.entity.JobStatus;
import com.hireconnect.jobservice.entity.Role;
import com.hireconnect.jobservice.exception.JobNotFoundException;
import com.hireconnect.jobservice.exception.UnauthorizedJobAccessException;
import com.hireconnect.jobservice.mapper.JobMapper;
import com.hireconnect.jobservice.repository.JobRepository;
import com.hireconnect.jobservice.repository.SavedJobRepository;

@ExtendWith(MockitoExtension.class)
@Disabled("Disabled on JDK 25 due Mockito/JaCoCo instrumentation incompatibility; replaced by JobServiceImplNoMockitoTest")
public class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private SavedJobRepository savedJobRepository;

    private JobMapper jobMapper;

    private JobServiceImpl jobService;

    private JobRequestDto jobRequestDto;
    private Job job;
    private JobResponseDto jobResponseDto;

    @BeforeEach
    void setUp() {
        jobRequestDto = new JobRequestDto();
        jobRequestDto.setTitle("Software Engineer");
        jobMapper = new JobMapper();
        jobService = new JobServiceImpl(jobRepository, jobMapper, savedJobRepository);

        job = new Job();
        job.setJobId(10L);
        job.setRecruiterId(2L);
        job.setTitle("Software Engineer");
        job.setStatus(JobStatus.OPEN);

        jobResponseDto = JobResponseDto.builder()
                .jobId(10L)
                .title("Software Engineer")
                .build();
    }

    @Test
    void createJob_Success() {
        when(jobRepository.save(any(Job.class))).thenReturn(job);

        JobResponseDto response = jobService.createJob(2L, Role.RECRUITER, jobRequestDto);

        assertNotNull(response);
        assertEquals(10L, response.getJobId());
        assertEquals("Software Engineer", response.getTitle());
    }

    @Test
    void createJob_Unauthorized_ThrowsException() {
        assertThrows(UnauthorizedJobAccessException.class, () -> 
            jobService.createJob(1L, Role.CANDIDATE, jobRequestDto));
    }

    @Test
    void updateJob_Success() {
        when(jobRepository.findByJobIdAndRecruiterId(10L, 2L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenReturn(job);

        JobResponseDto response = jobService.updateJob(10L, 2L, Role.RECRUITER, jobRequestDto);

        assertNotNull(response);
        assertEquals(10L, response.getJobId());
    }

    @Test
    void updateJob_NotFound_ThrowsException() {
        when(jobRepository.findByJobIdAndRecruiterId(10L, 2L)).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class, () -> 
            jobService.updateJob(10L, 2L, Role.RECRUITER, jobRequestDto));
    }

    @Test
    void deleteJob_Success() {
        when(jobRepository.findByJobIdAndRecruiterId(10L, 2L)).thenReturn(Optional.of(job));
        doNothing().when(jobRepository).delete(job);

        assertDoesNotThrow(() -> jobService.deleteJob(10L, 2L, Role.RECRUITER));
        verify(jobRepository, times(1)).delete(job);
    }

    @Test
    void getOpenJobById_Success() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));

        JobResponseDto response = jobService.getOpenJobById(10L);

        assertNotNull(response);
        assertEquals(10L, response.getJobId());
    }

    @Test
    void getOpenJobById_JobClosed_ThrowsException() {
        job.setStatus(JobStatus.CLOSED);
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));

        assertThrows(JobNotFoundException.class, () -> jobService.getOpenJobById(10L));
    }
}
