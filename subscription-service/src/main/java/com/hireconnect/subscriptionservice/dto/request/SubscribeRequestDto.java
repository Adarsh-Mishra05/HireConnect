package com.hireconnect.subscriptionservice.dto.request;

import com.hireconnect.subscriptionservice.enums.PaymentMode;
import com.hireconnect.subscriptionservice.enums.PlanTier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscribeRequestDto {

    @NotNull
    private PlanTier plan;

    @NotNull
    private PaymentMode paymentMode;

    @NotBlank
    private String transactionId;
}
