package com.hireconnect.paymentservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hireconnect.paymentservice.enums.PaymentPurpose;
import com.hireconnect.paymentservice.enums.PaymentStatus;
import com.hireconnect.paymentservice.enums.Role;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private PaymentPurpose purpose;

    private Long referenceId; // jobId or planId

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    private String currency;

    @Column(unique = true)
    private String providerOrderId;

    private String providerPaymentId;

    private String providerSignature;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}