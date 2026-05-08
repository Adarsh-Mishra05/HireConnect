package com.hireconnect.paymentservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hireconnect.paymentservice.enums.PaymentPurpose;
import com.hireconnect.paymentservice.enums.PaymentStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponseDto {
    private Long id;
    private PaymentPurpose purpose;
    private Long referenceId;
    private BigDecimal amount;
    private String currency;
    private String providerOrderId;
    private String providerPaymentId;
    private PaymentStatus status;
    private String description;
    private LocalDateTime createdAt;
}