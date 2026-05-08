package com.hireconnect.subscriptionservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hireconnect.subscriptionservice.enums.PaymentMode;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvoiceResponseDto {
    private Long id;
    private Long subscriptionId;
    private Long recruiterId;
    private BigDecimal amount;
    private PaymentMode paymentMode;
    private String transactionId;
    private LocalDateTime paymentDate;
}
