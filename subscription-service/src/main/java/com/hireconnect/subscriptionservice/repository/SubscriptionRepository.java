package com.hireconnect.subscriptionservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hireconnect.subscriptionservice.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByRecruiterIdOrderByCreatedAtDesc(Long recruiterId);

    Optional<Subscription> findFirstByRecruiterIdAndActiveTrueOrderByCreatedAtDesc(Long recruiterId);
}
