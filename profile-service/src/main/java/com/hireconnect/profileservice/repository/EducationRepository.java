package com.hireconnect.profileservice.repository;

import com.hireconnect.profileservice.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationRepository extends JpaRepository<Education, Long> {
}