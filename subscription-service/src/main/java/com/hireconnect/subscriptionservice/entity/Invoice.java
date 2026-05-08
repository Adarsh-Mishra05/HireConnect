package com.hireconnect.subscriptionservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hireconnect.subscriptionservice.enums.PaymentMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long subscriptionId;

    @Column(nullable = false)
    private Long recruiterId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMode paymentMode;

    @Column(nullable = false, length = 100)
    private String transactionId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime paymentDate;

    @PrePersist
    public void prePersist() {
        this.paymentDate = LocalDateTime.now();
    }
}
