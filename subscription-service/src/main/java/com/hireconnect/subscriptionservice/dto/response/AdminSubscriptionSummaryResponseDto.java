package com.hireconnect.subscriptionservice.dto.response;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminSubscriptionSummaryResponseDto {
    private long totalSubscriptions;
    private long activeSubscriptions;
    private long cancelledSubscriptions;
    private long totalInvoices;
    private BigDecimal totalInvoicedAmount;
}
