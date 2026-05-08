package com.hireconnect.paymentservice.dto.request;

import java.math.BigDecimal;

import com.hireconnect.paymentservice.enums.PaymentPurpose;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentOrderRequestDto {

    @NotNull
    private PaymentPurpose purpose;

    @NotNull
    private Long referenceId;

    @NotNull
    @DecimalMin(value = "1.00")
    private BigDecimal amount;

    private String description;
}