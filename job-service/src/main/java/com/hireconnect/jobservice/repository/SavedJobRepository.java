package com.hireconnect.jobservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hireconnect.jobservice.entity.SavedJob;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    Optional<SavedJob> findByCandidateIdAndJobId(Long candidateId, Long jobId);

    List<SavedJob> findByCandidateIdOrderBySavedAtDesc(Long candidateId);

    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);
}
