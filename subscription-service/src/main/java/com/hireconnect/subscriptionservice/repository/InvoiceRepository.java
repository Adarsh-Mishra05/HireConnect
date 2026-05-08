package com.hireconnect.subscriptionservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hireconnect.subscriptionservice.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByRecruiterIdOrderByPaymentDateDesc(Long recruiterId);

    List<Invoice> findBySubscriptionIdOrderByPaymentDateDesc(Long subscriptionId);
}
