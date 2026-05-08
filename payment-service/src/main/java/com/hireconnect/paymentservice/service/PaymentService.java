package com.hireconnect.paymentservice.service;

import java.util.List;

import com.hireconnect.paymentservice.dto.request.CreatePaymentOrderRequestDto;
import com.hireconnect.paymentservice.dto.request.VerifyPaymentRequestDto;
import com.hireconnect.paymentservice.dto.response.AdminPaymentSummaryResponseDto;
import com.hireconnect.paymentservice.dto.response.PaymentOrderResponseDto;
import com.hireconnect.paymentservice.dto.response.PaymentResponseDto;
import com.hireconnect.paymentservice.security.AuthenticatedUser;


public interface PaymentService {
    PaymentOrderResponseDto createOrder(AuthenticatedUser user, CreatePaymentOrderRequestDto request);
    PaymentResponseDto verifyPayment(AuthenticatedUser user, VerifyPaymentRequestDto request);
    List<PaymentResponseDto> getMyPayments(AuthenticatedUser user);
    List<PaymentResponseDto> getAllPaymentsForAdmin(AuthenticatedUser user);
    AdminPaymentSummaryResponseDto getAdminPaymentSummary(AuthenticatedUser user);
    void handleWebhook(String payload, String signature);
}
