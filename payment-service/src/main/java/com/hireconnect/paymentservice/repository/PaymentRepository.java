package com.hireconnect.paymentservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hireconnect.paymentservice.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByProviderOrderId(String providerOrderId);
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Payment> findAllByOrderByCreatedAtDesc();
}
