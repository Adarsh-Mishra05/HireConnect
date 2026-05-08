package com.hireconnect.applicationservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hireconnect.applicationservice.entity.JobApplication;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    List<JobApplication> findByCandidateIdOrderByAppliedAtDesc(Long candidateId);

    Optional<JobApplication> findByIdAndCandidateId(Long id, Long candidateId);

    List<JobApplication> findByJobIdInOrderByAppliedAtDesc(List<Long> jobIds);
    
    List<JobApplication> findByJobIdOrderByAppliedAtDesc(Long jobId);

    Optional<JobApplication> findByJobIdAndCandidateId(Long jobId, Long candidateId);

    List<JobApplication> findByRecruiterIdOrderByAppliedAtDesc(Long recruiterId);

    List<JobApplication> findAllByOrderByAppliedAtDesc();
    
    Boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);
    
}
