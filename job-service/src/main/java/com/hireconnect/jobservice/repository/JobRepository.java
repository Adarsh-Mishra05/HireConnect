package com.hireconnect.jobservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.hireconnect.jobservice.entity.Job;
import com.hireconnect.jobservice.entity.JobStatus;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    List<Job> findByRecruiterIdOrderByCreatedAtDesc(Long recruiterId);

    Optional<Job> findByJobIdAndRecruiterId(Long jobId, Long recruiterId);

    boolean existsByJobId(Long jobId);

    boolean existsByJobIdAndStatus(Long jobId, JobStatus status);
}