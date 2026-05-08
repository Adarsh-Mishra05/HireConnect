package com.hireconnect.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hireconnect.auth.entity.PasswordResetOtp;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findTopByEmailAndOtpAndUsedFalseOrderByIdDesc(String email, String otp);

    void deleteByEmail(String email);
}