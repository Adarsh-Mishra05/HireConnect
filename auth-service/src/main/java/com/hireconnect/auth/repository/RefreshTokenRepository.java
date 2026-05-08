package com.hireconnect.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hireconnect.auth.entity.RefreshToken;
import com.hireconnect.auth.entity.UserCredential;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(UserCredential user);

    void deleteByUser(UserCredential user);
}