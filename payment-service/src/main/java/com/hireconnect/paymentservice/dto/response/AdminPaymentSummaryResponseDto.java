package com.hireconnect.paymentservice.dto.response;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminPaymentSummaryResponseDto {
    private long totalTransactions;
    private long successfulTransactions;
    private long failedTransactions;
    private long pendingTransactions;
    private BigDecimal totalRevenue;
}
