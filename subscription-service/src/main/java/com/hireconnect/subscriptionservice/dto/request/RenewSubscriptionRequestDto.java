package com.hireconnect.subscriptionservice.dto.request;

import com.hireconnect.subscriptionservice.enums.PaymentMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenewSubscriptionRequestDto {

    @NotNull
    private PaymentMode paymentMode;

    @NotBlank
    private String transactionId;
}
