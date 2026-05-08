package com.hireconnect.profileservice.repository;

import com.hireconnect.profileservice.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
}