package com.hireconnect.profileservice.repository;

import com.hireconnect.profileservice.entity.SocialLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {
}