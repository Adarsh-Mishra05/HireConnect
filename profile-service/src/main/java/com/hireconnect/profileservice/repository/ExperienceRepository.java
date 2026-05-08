package com.hireconnect.profileservice.repository;

import com.hireconnect.profileservice.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
}