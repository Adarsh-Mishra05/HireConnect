package com.hireconnect.paymentservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hireconnect.paymentservice.config.RazorpayProperties;
import com.hireconnect.paymentservice.dto.request.VerifyPaymentRequestDto;
import com.hireconnect.paymentservice.dto.response.PaymentResponseDto;
import com.hireconnect.paymentservice.entity.Payment;
import com.hireconnect.paymentservice.enums.PaymentStatus;
import com.hireconnect.paymentservice.enums.Role;
import com.hireconnect.paymentservice.repository.PaymentRepository;
import com.hireconnect.paymentservice.security.AuthenticatedUser;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest{

    @Mock
    private PaymentRepository paymentRepository;

    private RazorpayProperties razorpayProperties;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private AuthenticatedUser user;
    private Payment payment;

    @BeforeEach
    void setUp() {
        razorpayProperties = new RazorpayProperties();
        user = new AuthenticatedUser(1L, "user@example.com", Role.CANDIDATE);

        payment = Payment.builder()
                .id(10L)
                .userId(1L)
                .amount(BigDecimal.valueOf(100.0))
                .providerOrderId("order_xyz")
                .status(PaymentStatus.CREATED)
                .build();
    }

    @Test
    void getMyPayments_Success() {
        when(paymentRepository.findByUserIdOrderByCreatedAtDesc(1L))
            .thenReturn(List.of(payment));

        List<PaymentResponseDto> responses = paymentService.getMyPayments(user);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(10L, responses.get(0).getId());
    }

    @Test
    void verifyPayment_Unauthorized_ThrowsException() {
        VerifyPaymentRequestDto request = new VerifyPaymentRequestDto();
        request.setRazorpayOrderId("order_xyz");

        payment.setUserId(2L); // Different user
        when(paymentRepository.findByProviderOrderId("order_xyz")).thenReturn(Optional.of(payment));

        assertThrows(RuntimeException.class, () -> 
            paymentService.verifyPayment(user, request));
    }

    @Test
    void verifyPayment_NotFound_ThrowsException() {
        VerifyPaymentRequestDto request = new VerifyPaymentRequestDto();
        request.setRazorpayOrderId("order_xyz");

        when(paymentRepository.findByProviderOrderId("order_xyz")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            paymentService.verifyPayment(user, request));
    }
}
