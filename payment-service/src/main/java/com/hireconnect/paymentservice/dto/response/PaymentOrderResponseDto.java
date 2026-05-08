package com.hireconnect.paymentservice.dto.response;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentOrderResponseDto {
    private Long paymentId;
    private String keyId;
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String email;
}