package com.hireconnect.interviewservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hireconnect.interviewservice.entity.Interview;
import com.hireconnect.interviewservice.enums.InterviewStatus;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByCandidateIdOrderByScheduledAtDesc(Long candidateId);

    List<Interview> findByRecruiterIdOrderByScheduledAtDesc(Long recruiterId);

    List<Interview> findByApplicationIdOrderByScheduledAtDesc(Long applicationId);

    Optional<Interview> findByIdAndCandidateId(Long id, Long candidateId);

    Optional<Interview> findByIdAndRecruiterId(Long id, Long recruiterId);

    boolean existsByApplicationIdAndStatusIn(Long applicationId, List<InterviewStatus> statuses);
}