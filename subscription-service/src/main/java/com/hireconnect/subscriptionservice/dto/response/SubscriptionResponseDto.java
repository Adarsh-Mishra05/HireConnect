package com.hireconnect.subscriptionservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.hireconnect.subscriptionservice.enums.PlanTier;
import com.hireconnect.subscriptionservice.enums.SubscriptionStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionResponseDto {
    private Long id;
    private Long recruiterId;
    private PlanTier plan;
    private LocalDate startDate;
    private LocalDate endDate;
    private SubscriptionStatus status;
    private BigDecimal amountPaid;
    private Boolean active;
}
