package com.hireconnect.jobservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hireconnect.jobservice.dto.request.JobRequestDto;
import com.hireconnect.jobservice.dto.response.JobResponseDto;
import com.hireconnect.jobservice.entity.ExperienceLevel;
import com.hireconnect.jobservice.entity.Job;
import com.hireconnect.jobservice.entity.JobStatus;
import com.hireconnect.jobservice.entity.JobType;
import com.hireconnect.jobservice.entity.Role;
import com.hireconnect.jobservice.entity.SavedJob;
import com.hireconnect.jobservice.exception.JobNotFoundException;
import com.hireconnect.jobservice.exception.UnauthorizedJobAccessException;
import com.hireconnect.jobservice.mapper.JobMapper;
import com.hireconnect.jobservice.repository.JobRepository;
import com.hireconnect.jobservice.repository.SavedJobRepository;

class JobServiceImplNoMockitoTest {

    private final AtomicLong jobIdSeq = new AtomicLong(100);
    private final AtomicLong savedIdSeq = new AtomicLong(200);
    private final Map<Long, Job> jobs = new HashMap<>();
    private final List<SavedJob> savedJobs = new ArrayList<>();

    private JobServiceImpl service;

    @BeforeEach
    void setUp() {
        jobs.clear();
        savedJobs.clear();

        JobRepository jobRepository = (JobRepository) Proxy.newProxyInstance(
                JobRepository.class.getClassLoader(),
                new Class[]{JobRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        Job job = (Job) args[0];
                        if (job.getJobId() == null) {
                            job.setJobId(jobIdSeq.incrementAndGet());
                        }
                        jobs.put(job.getJobId(), job);
                        return job;
                    }
                    if ("findById".equals(name)) {
                        return Optional.ofNullable(jobs.get((Long) args[0]));
                    }
                    if ("findByJobIdAndRecruiterId".equals(name)) {
                        Long jobId = (Long) args[0];
                        Long recruiterId = (Long) args[1];
                        Job job = jobs.get(jobId);
                        return Optional.ofNullable(job != null && recruiterId.equals(job.getRecruiterId()) ? job : null);
                    }
                    if ("findByRecruiterIdOrderByCreatedAtDesc".equals(name)) {
                        Long recruiterId = (Long) args[0];
                        return jobs.values().stream()
                                .filter(j -> recruiterId.equals(j.getRecruiterId()))
                                .sorted(Comparator.comparing(Job::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                                .toList();
                    }
                    if ("findAll".equals(name)) {
                        return new ArrayList<>(jobs.values());
                    }
                    if ("existsByJobId".equals(name)) {
                        return jobs.containsKey((Long) args[0]);
                    }
                    if ("existsByJobIdAndStatus".equals(name)) {
                        Job job = jobs.get((Long) args[0]);
                        return job != null && job.getStatus() == args[1];
                    }
                    if ("delete".equals(name)) {
                        Job job = (Job) args[0];
                        jobs.remove(job.getJobId());
                        return null;
                    }
                    if ("count".equals(name)) {
                        return (long) jobs.size();
                    }
                    if ("toString".equals(name)) {
                        return "FakeJobRepository";
                    }
                    throw new UnsupportedOperationException("Unhandled method: " + name);
                });

        SavedJobRepository savedJobRepository = (SavedJobRepository) Proxy.newProxyInstance(
                SavedJobRepository.class.getClassLoader(),
                new Class[]{SavedJobRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        SavedJob saved = (SavedJob) args[0];
                        if (saved.getId() == null) {
                            saved.setId(savedIdSeq.incrementAndGet());
                        }
                        if (saved.getSavedAt() == null) {
                            saved.setSavedAt(LocalDateTime.now());
                        }
                        savedJobs.removeIf(s -> s.getCandidateId().equals(saved.getCandidateId())
                                && s.getJobId().equals(saved.getJobId()));
                        savedJobs.add(saved);
                        return saved;
                    }
                    if ("existsByCandidateIdAndJobId".equals(name)) {
                        Long candidateId = (Long) args[0];
                        Long jobId = (Long) args[1];
                        return savedJobs.stream().anyMatch(s -> s.getCandidateId().equals(candidateId) && s.getJobId().equals(jobId));
                    }
                    if ("findByCandidateIdAndJobId".equals(name)) {
                        Long candidateId = (Long) args[0];
                        Long jobId = (Long) args[1];
                        return savedJobs.stream()
                                .filter(s -> s.getCandidateId().equals(candidateId) && s.getJobId().equals(jobId))
                                .findFirst();
                    }
                    if ("findByCandidateIdOrderBySavedAtDesc".equals(name)) {
                        Long candidateId = (Long) args[0];
                        return savedJobs.stream()
                                .filter(s -> s.getCandidateId().equals(candidateId))
                                .sorted(Comparator.comparing(SavedJob::getSavedAt).reversed())
                                .toList();
                    }
                    if ("delete".equals(name)) {
                        SavedJob saved = (SavedJob) args[0];
                        savedJobs.removeIf(s -> s.getId().equals(saved.getId()));
                        return null;
                    }
                    if ("toString".equals(name)) {
                        return "FakeSavedJobRepository";
                    }
                    throw new UnsupportedOperationException("Unhandled method: " + name);
                });

        service = new JobServiceImpl(jobRepository, new JobMapper(), savedJobRepository);
    }

    @Test
    void createUpdateAndDeleteFlow_WorksForRecruiter() {
        JobRequestDto request = request("Backend Engineer", JobStatus.OPEN);
        JobResponseDto created = service.createJob(11L, Role.RECRUITER, request);
        assertNotNull(created.getJobId());
        assertEquals("Backend Engineer", created.getTitle());

        JobRequestDto updatedRequest = request("Senior Backend Engineer", JobStatus.CLOSED);
        JobResponseDto updated = service.updateJob(created.getJobId(), 11L, Role.RECRUITER, updatedRequest);
        assertEquals("Senior Backend Engineer", updated.getTitle());
        assertEquals(JobStatus.CLOSED, updated.getStatus());

        service.deleteJob(created.getJobId(), 11L, Role.RECRUITER);
        assertFalse(service.doesJobExist(created.getJobId()));
    }

    @Test
    void recruiterAuthorization_IsEnforced() {
        JobRequestDto request = request("Data Engineer", JobStatus.OPEN);
        assertThrows(UnauthorizedJobAccessException.class,
                () -> service.createJob(11L, Role.CANDIDATE, request));
        assertThrows(UnauthorizedJobAccessException.class,
                () -> service.getMyJobs(11L, Role.ADMIN));
    }

    @Test
    void openJobQueriesAndExistenceChecks_Work() {
        Long openJobId = service.createJob(21L, Role.RECRUITER, request("Java Dev", JobStatus.OPEN)).getJobId();
        Long closedJobId = service.createJob(21L, Role.RECRUITER, request("Old Java Dev", JobStatus.CLOSED)).getJobId();

        assertTrue(service.doesJobExist(openJobId));
        assertTrue(service.isJobOpen(openJobId));
        assertFalse(service.isJobOpen(closedJobId));
        assertTrue(service.isJobOwnedByRecruiter(openJobId, 21L));

        List<JobResponseDto> open = service.getAllOpenJobs();
        assertEquals(1, open.size());
        assertEquals(openJobId, open.get(0).getJobId());

        assertThrows(JobNotFoundException.class, () -> service.getOpenJobById(closedJobId));
    }

    @Test
    void adminActions_UpdateStatusDeleteAndFetchAll_Work() {
        Long jobId = service.createJob(31L, Role.RECRUITER, request("SRE", JobStatus.OPEN)).getJobId();

        JobResponseDto updated = service.updateJobStatusByAdmin(1L, Role.ADMIN, jobId, JobStatus.CLOSED);
        assertEquals(JobStatus.CLOSED, updated.getStatus());
        assertEquals(1, service.getAllJobsForAdmin(1L, Role.ADMIN).size());

        assertThrows(UnauthorizedJobAccessException.class,
                () -> service.updateJobStatusByAdmin(2L, Role.RECRUITER, jobId, JobStatus.OPEN));
        assertThrows(UnauthorizedJobAccessException.class,
                () -> service.deleteJobByAdmin(2L, Role.RECRUITER, jobId));

        service.deleteJobByAdmin(1L, Role.ADMIN, jobId);
        assertFalse(service.doesJobExist(jobId));
    }

    @Test
    void featuredAndSavedJobsFlow_Works() {
        Long openJobId = service.createJob(41L, Role.RECRUITER, request("Mobile Dev", JobStatus.OPEN)).getJobId();
        Long closedJobId = service.createJob(41L, Role.RECRUITER, request("Legacy Mobile Dev", JobStatus.CLOSED)).getJobId();

        service.markAsFeatured(openJobId, 41L, Role.RECRUITER);
        assertEquals(41L, service.getRecruiterIdByJobId(openJobId));
        assertEquals(2, service.getJobIdsByRecruiter(41L).size());
        assertEquals(2, service.countAllJobs());

        service.saveJobForCandidate(openJobId, 51L, Role.CANDIDATE);
        service.saveJobForCandidate(openJobId, 51L, Role.CANDIDATE);
        assertEquals(1, service.getSavedJobsForCandidate(51L, Role.CANDIDATE).size());

        assertThrows(JobNotFoundException.class,
                () -> service.saveJobForCandidate(closedJobId, 51L, Role.CANDIDATE));
        assertThrows(UnauthorizedJobAccessException.class,
                () -> service.saveJobForCandidate(openJobId, 51L, Role.RECRUITER));
        assertThrows(UnauthorizedJobAccessException.class,
                () -> service.getSavedJobsForCandidate(51L, Role.RECRUITER));

        service.removeSavedJobForCandidate(openJobId, 51L, Role.CANDIDATE);
        assertEquals(0, service.getSavedJobsForCandidate(51L, Role.CANDIDATE).size());
    }

    private JobRequestDto request(String title, JobStatus status) {
        JobRequestDto dto = new JobRequestDto();
        dto.setTitle(title);
        dto.setDescription("desc " + title);
        dto.setCompanyName("HireConnect");
        dto.setLocation("Pune");
        dto.setJobType(JobType.FULL_TIME);
        dto.setExperienceLevel(ExperienceLevel.MID);
        dto.setSalaryMin(10.0);
        dto.setSalaryMax(20.0);
        dto.setSkillsRequired("Java,Spring");
        dto.setStatus(status);
        return dto;
    }
}

