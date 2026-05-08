package com.hireconnect.paymentservice.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hireconnect.paymentservice.config.RazorpayProperties;
import com.hireconnect.paymentservice.dto.request.CreatePaymentOrderRequestDto;
import com.hireconnect.paymentservice.dto.request.VerifyPaymentRequestDto;
import com.hireconnect.paymentservice.dto.response.AdminPaymentSummaryResponseDto;
import com.hireconnect.paymentservice.dto.response.PaymentOrderResponseDto;
import com.hireconnect.paymentservice.dto.response.PaymentResponseDto;
import com.hireconnect.paymentservice.entity.Payment;
import com.hireconnect.paymentservice.enums.PaymentStatus;
import com.hireconnect.paymentservice.enums.Role;
import com.hireconnect.paymentservice.repository.PaymentRepository;
import com.hireconnect.paymentservice.security.AuthenticatedUser;
import com.hireconnect.paymentservice.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final RazorpayProperties razorpayProperties;

    @Override
    @Transactional
    public PaymentOrderResponseDto createOrder(AuthenticatedUser user, CreatePaymentOrderRequestDto request) {
        logger.info("Creating payment order for userId={}, purpose={}, referenceId={}, amount={}",
                user.userId(), request.getPurpose(), request.getReferenceId(), request.getAmount());

        try {
            RazorpayClient razorpayClient =
                    new RazorpayClient(razorpayProperties.getKeyId(), razorpayProperties.getKeySecret());

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", request.getAmount().multiply(java.math.BigDecimal.valueOf(100)).intValue());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "hc_" + user.userId() + "_" + System.currentTimeMillis());

            Order order = razorpayClient.orders.create(orderRequest);

            Payment payment = Payment.builder()
                    .userId(user.userId())
                    .email(user.email())
                    .role(user.role())
                    .purpose(request.getPurpose())
                    .referenceId(request.getReferenceId())
                    .amount(request.getAmount())
                    .currency("INR")
                    .providerOrderId(order.get("id"))
                    .status(PaymentStatus.CREATED)
                    .description(request.getDescription())
                    .build();

            paymentRepository.save(payment);
            logger.info("Payment order created successfully. paymentId={}, providerOrderId={}",
                    payment.getId(), payment.getProviderOrderId());

            return PaymentOrderResponseDto.builder()
                    .paymentId(payment.getId())
                    .keyId(razorpayProperties.getKeyId())
                    .orderId(payment.getProviderOrderId())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .description(payment.getDescription())
                    .email(payment.getEmail())
                    .build();

        } catch (Exception e) {
            logger.error("Failed to create payment order for userId={}", user.userId(), e);
            throw new RuntimeException("Failed to create payment order", e);
        }
    }

    @Override
    @Transactional
    public PaymentResponseDto verifyPayment(AuthenticatedUser user, VerifyPaymentRequestDto request) {
        logger.info("Verifying payment for userId={}, orderId={}", user.userId(), request.getRazorpayOrderId());

        Payment payment = paymentRepository.findByProviderOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Payment order not found"));

        if (!payment.getUserId().equals(user.userId())) {
            logger.warn("Unauthorized payment verification attempt. paymentUserId={}, requesterUserId={}",
                    payment.getUserId(), user.userId());
            throw new RuntimeException("Unauthorized payment access");
        }

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean valid = Utils.verifyPaymentSignature(options, razorpayProperties.getKeySecret());

            if (!valid) {
                logger.warn("Invalid payment signature for orderId={}", request.getRazorpayOrderId());
                throw new RuntimeException("Invalid payment signature");
            }

            payment.setProviderPaymentId(request.getRazorpayPaymentId());
            payment.setProviderSignature(request.getRazorpaySignature());
            payment.setStatus(PaymentStatus.SUCCESS);

            paymentRepository.save(payment);
            logger.info("Payment verified successfully. paymentId={}, providerPaymentId={}",
                    payment.getId(), payment.getProviderPaymentId());
            return map(payment);

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            logger.error("Payment verification failed for orderId={}", request.getRazorpayOrderId(), e);
            throw new RuntimeException("Payment verification failed", e);
        }
    }

    @Override
    public List<PaymentResponseDto> getMyPayments(AuthenticatedUser user) {
        logger.info("Fetching payment history for userId={}", user.userId());
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(user.userId())
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public List<PaymentResponseDto> getAllPaymentsForAdmin(AuthenticatedUser user) {
        if (user.role() != Role.ADMIN) {
            throw new RuntimeException("Only admins can perform this action");
        }

        logger.info("Fetching all payments for adminId={}", user.userId());
        return paymentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public AdminPaymentSummaryResponseDto getAdminPaymentSummary(AuthenticatedUser user) {
        if (user.role() != Role.ADMIN) {
            throw new RuntimeException("Only admins can perform this action");
        }

        List<Payment> allPayments = paymentRepository.findAllByOrderByCreatedAtDesc();
        long successful = allPayments.stream().filter(payment -> payment.getStatus() == PaymentStatus.SUCCESS).count();
        long failed = allPayments.stream().filter(payment -> payment.getStatus() == PaymentStatus.FAILED).count();
        long pending = allPayments.stream().filter(payment -> payment.getStatus() == PaymentStatus.CREATED).count();

        java.math.BigDecimal totalRevenue = allPayments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return AdminPaymentSummaryResponseDto.builder()
                .totalTransactions(allPayments.size())
                .successfulTransactions(successful)
                .failedTransactions(failed)
                .pendingTransactions(pending)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) {
        logger.info("Processing payment webhook");

        if (!isValidWebhookSignature(payload, signature, razorpayProperties.getWebhookSecret())) {
            logger.warn("Invalid webhook signature received");
            throw new RuntimeException("Invalid webhook signature");
        }

        JSONObject json = new JSONObject(payload);
        String event = json.getString("event");

        logger.info("Webhook event received: {}", event);

        if ("order.paid".equals(event)) {
            JSONObject paymentEntity = json
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String orderId = paymentEntity.getString("order_id");
            String paymentId = paymentEntity.getString("id");

            paymentRepository.findByProviderOrderId(orderId).ifPresent(payment -> {
                logger.info("Marking payment success from webhook. paymentId={}, orderId={}, paymentIdProvider={}",
                        payment.getId(), orderId, paymentId);
                payment.setProviderPaymentId(paymentId);
                payment.setStatus(PaymentStatus.SUCCESS);
                paymentRepository.save(payment);
            });
        } else {
            logger.info("Ignoring unsupported webhook event: {}", event);
        }
    }

    private boolean isValidWebhookSignature(String payload, String actualSignature, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString().equals(actualSignature);
        } catch (Exception e) {
            logger.error("Error while validating webhook signature", e);
            return false;
        }
    }

    private PaymentResponseDto map(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .purpose(payment.getPurpose())
                .referenceId(payment.getReferenceId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .providerOrderId(payment.getProviderOrderId())
                .providerPaymentId(payment.getProviderPaymentId())
                .status(payment.getStatus())
                .description(payment.getDescription())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
