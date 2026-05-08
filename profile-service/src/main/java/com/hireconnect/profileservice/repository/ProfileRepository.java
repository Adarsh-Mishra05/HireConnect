package com.hireconnect.profileservice.repository;

import com.hireconnect.profileservice.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
 * full profile will mostly be handled through ProfileRepository
 * Because of cascade = ALL, saving profile can save children too
 * So child repositories are mainly there for flexibility and future use.
 */

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}