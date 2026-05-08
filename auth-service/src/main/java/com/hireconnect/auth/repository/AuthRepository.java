package com.hireconnect.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hireconnect.auth.entity.UserCredential;

public interface AuthRepository extends JpaRepository<UserCredential, Long> {

    Optional<UserCredential> findByEmail(String email);

    boolean existsByEmail(String email);
}