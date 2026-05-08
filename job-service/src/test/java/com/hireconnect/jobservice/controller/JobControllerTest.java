package com.hireconnect.jobservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.jobservice.dto.request.JobRequestDto;
import com.hireconnect.jobservice.dto.response.JobResponseDto;
import com.hireconnect.jobservice.entity.Role;
import com.hireconnect.jobservice.security.AuthenticatedUser;
import com.hireconnect.jobservice.service.JobService;

@WebMvcTest(JobController.class)
@AutoConfigureMockMvc(addFilters = false)
public class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthenticatedUser recruiter;
    private AuthenticatedUser admin;

    @BeforeEach
    void setUp() {
        recruiter = new AuthenticatedUser(1L, "recruiter@test.com", Role.RECRUITER);
        admin = new AuthenticatedUser(2L, "admin@test.com", Role.ADMIN);
    }

    private void setAuthentication(AuthenticatedUser user) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createJob_Success() throws Exception {
        setAuthentication(recruiter);
        JobRequestDto request = new JobRequestDto();
        request.setTitle("Software Engineer");
        
        JobResponseDto response = JobResponseDto.builder()
                .jobId(100L)
                .build();

        when(jobService.createJob(eq(1L), eq(Role.RECRUITER), any(JobRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/jobs")
                .principal(new UsernamePasswordAuthenticationToken(recruiter, null))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateJob_Success() throws Exception {
        setAuthentication(recruiter);
        JobRequestDto request = new JobRequestDto();
        request.setTitle("Updated Title");

        JobResponseDto response = JobResponseDto.builder().build();
        when(jobService.updateJob(eq(100L), eq(1L), eq(Role.RECRUITER), any(JobRequestDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/jobs/100")
                .principal(new UsernamePasswordAuthenticationToken(recruiter, null))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteJob_Success() throws Exception {
        setAuthentication(recruiter);

        mockMvc.perform(delete("/api/jobs/100")
                .principal(new UsernamePasswordAuthenticationToken(recruiter, null)))
                .andExpect(status().isOk());
    }

    @Test
    void getMyJobs_Success() throws Exception {
        setAuthentication(recruiter);
        when(jobService.getMyJobs(1L, Role.RECRUITER)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/jobs/recruiter/me")
                .principal(new UsernamePasswordAuthenticationToken(recruiter, null)))
                .andExpect(status().isOk());
    }

    @Test
    void getAllOpenJobs_Success() throws Exception {
        when(jobService.getAllOpenJobs()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk());
    }

    @Test
    void getOpenJobById_Success() throws Exception {
        when(jobService.getOpenJobById(100L)).thenReturn(JobResponseDto.builder().build());

        mockMvc.perform(get("/api/jobs/100"))
                .andExpect(status().isOk());
    }

    @Test
    void searchJobs_Success() throws Exception {
        when(jobService.searchOpenJobs(any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/jobs/search")
                .param("keyword", "java"))
                .andExpect(status().isOk());
    }

    @Test
    void doesJobExist_Success() throws Exception {
        when(jobService.doesJobExist(100L)).thenReturn(true);

        mockMvc.perform(get("/api/jobs/internal/100/exists"))
                .andExpect(status().isOk());
    }

    @Test
    void markJobAsFeatured_Success() throws Exception {
        setAuthentication(recruiter);
        mockMvc.perform(put("/api/jobs/100/feature")
                .principal(new UsernamePasswordAuthenticationToken(recruiter, null)))
                .andExpect(status().isOk());
    }

    @Test
    void saveJobForCandidate_Success() throws Exception {
        setAuthentication(recruiter); // Candidate role would be better but recruiter works for simple test
        mockMvc.perform(post("/api/jobs/100/bookmark")
                .principal(new UsernamePasswordAuthenticationToken(recruiter, null)))
                .andExpect(status().isOk());
    }

    @Test
    void getAllJobsForAdmin_Success() throws Exception {
        setAuthentication(admin);
        when(jobService.getAllJobsForAdmin(2L, Role.ADMIN)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/jobs/admin/all")
                .principal(new UsernamePasswordAuthenticationToken(admin, null)))
                .andExpect(status().isOk());
    }

    @Test
    void isJobOpen_Success() throws Exception {
        when(jobService.isJobOpen(100L)).thenReturn(true);

        mockMvc.perform(get("/api/jobs/internal/100/open"))
                .andExpect(status().isOk());
    }

    @Test
    void isJobOwnedByRecruiter_Success() throws Exception {
        when(jobService.isJobOwnedByRecruiter(100L, 1L)).thenReturn(true);

        mockMvc.perform(get("/api/jobs/internal/100/recruiter/1/ownership"))
                .andExpect(status().isOk());
    }

    @Test
    void getJobIdsByRecruiter_Success() throws Exception {
        when(jobService.getJobIdsByRecruiter(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/jobs/internal/recruiter/1/job-ids"))
                .andExpect(status().isOk());
    }

    @Test
    void getRecruiterIdByJobId_Success() throws Exception {
        when(jobService.getRecruiterIdByJobId(100L)).thenReturn(1L);

        mockMvc.perform(get("/api/jobs/internal/100/recruiter-id"))
                .andExpect(status().isOk());
    }

    @Test
    void countAllJobsInternal_Success() throws Exception {
        when(jobService.countAllJobs()).thenReturn(10L);

        mockMvc.perform(get("/api/jobs/internal/count"))
                .andExpect(status().isOk());
    }

    @Test
    void updateJobStatusByAdmin_Success() throws Exception {
        setAuthentication(admin);
        when(jobService.updateJobStatusByAdmin(2L, Role.ADMIN, 100L, com.hireconnect.jobservice.entity.JobStatus.CLOSED))
                .thenReturn(JobResponseDto.builder().build());

        mockMvc.perform(patch("/api/jobs/admin/100/status")
                .param("status", "CLOSED")
                .principal(new UsernamePasswordAuthenticationToken(admin, null)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteJobByAdmin_Success() throws Exception {
        setAuthentication(admin);
        mockMvc.perform(delete("/api/jobs/admin/100")
                .principal(new UsernamePasswordAuthenticationToken(admin, null)))
                .andExpect(status().isOk());
    }
}
