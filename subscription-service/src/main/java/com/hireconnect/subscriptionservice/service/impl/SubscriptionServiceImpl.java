package com.hireconnect.subscriptionservice.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hireconnect.subscriptionservice.dto.request.RenewSubscriptionRequestDto;
import com.hireconnect.subscriptionservice.dto.request.SubscribeRequestDto;
import com.hireconnect.subscriptionservice.dto.response.AdminSubscriptionSummaryResponseDto;
import com.hireconnect.subscriptionservice.dto.response.InvoiceResponseDto;
import com.hireconnect.subscriptionservice.dto.response.SubscriptionResponseDto;
import com.hireconnect.subscriptionservice.entity.Invoice;
import com.hireconnect.subscriptionservice.entity.Subscription;
import com.hireconnect.subscriptionservice.enums.PlanTier;
import com.hireconnect.subscriptionservice.enums.Role;
import com.hireconnect.subscriptionservice.enums.SubscriptionStatus;
import com.hireconnect.subscriptionservice.exception.BadRequestException;
import com.hireconnect.subscriptionservice.exception.ResourceNotFoundException;
import com.hireconnect.subscriptionservice.exception.UnauthorizedException;
import com.hireconnect.subscriptionservice.repository.InvoiceRepository;
import com.hireconnect.subscriptionservice.repository.SubscriptionRepository;
import com.hireconnect.subscriptionservice.security.AuthenticatedUser;
import com.hireconnect.subscriptionservice.service.SubscriptionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional
    public SubscriptionResponseDto subscribe(AuthenticatedUser user, SubscribeRequestDto requestDto) {
        validateRecruiter(user);
        validateSubscriptionPaymentInput(requestDto.getPlan(), requestDto.getPaymentMode(), requestDto.getTransactionId());

        subscriptionRepository.findFirstByRecruiterIdAndActiveTrueOrderByCreatedAtDesc(user.userId())
                .ifPresent(active -> {
                    throw new BadRequestException("Active subscription already exists");
                });

        BigDecimal amount = getPlanAmount(requestDto.getPlan());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(getPlanDurationDays(requestDto.getPlan()));

        Subscription subscription = Subscription.builder()
                .recruiterId(user.userId())
                .plan(requestDto.getPlan())
                .startDate(startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.ACTIVE)
                .amountPaid(amount)
                .active(true)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);
        createInvoice(saved, amount, resolvePaymentModeForInvoice(requestDto.getPlan(), requestDto.getPaymentMode()), requestDto.getTransactionId());
        return toSubscriptionResponse(saved);
    }

    @Override
    @Transactional
    public SubscriptionResponseDto cancelSubscription(AuthenticatedUser user) {
        validateRecruiter(user);

        Subscription active = subscriptionRepository.findFirstByRecruiterIdAndActiveTrueOrderByCreatedAtDesc(user.userId())
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

        active.setActive(false);
        active.setStatus(SubscriptionStatus.CANCELLED);
        Subscription updated = subscriptionRepository.save(active);
        return toSubscriptionResponse(updated);
    }

    @Override
    @Transactional
    public SubscriptionResponseDto renewSubscription(AuthenticatedUser user, RenewSubscriptionRequestDto requestDto) {
        validateRecruiter(user);

        Subscription latest = subscriptionRepository.findFirstByRecruiterIdAndActiveTrueOrderByCreatedAtDesc(user.userId())
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

        LocalDate renewedStart = latest.getEndDate().isAfter(LocalDate.now()) ? latest.getEndDate() : LocalDate.now();
        latest.setEndDate(renewedStart.plusDays(getPlanDurationDays(latest.getPlan())));
        latest.setStatus(SubscriptionStatus.ACTIVE);
        latest.setActive(true);

        Subscription updated = subscriptionRepository.save(latest);
        createInvoice(updated, getPlanAmount(updated.getPlan()), requestDto.getPaymentMode(), requestDto.getTransactionId());
        return toSubscriptionResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponseDto getMyActiveSubscription(AuthenticatedUser user) {
        validateRecruiter(user);
        Subscription subscription = subscriptionRepository.findFirstByRecruiterIdAndActiveTrueOrderByCreatedAtDesc(user.userId())
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));
        return toSubscriptionResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponseDto> getMyInvoices(AuthenticatedUser user) {
        validateRecruiter(user);
        return invoiceRepository.findByRecruiterIdOrderByPaymentDateDesc(user.userId())
                .stream()
                .map(this::toInvoiceResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponseDto> getAllSubscriptionsForAdmin(AuthenticatedUser user) {
        validateAdmin(user);
        return subscriptionRepository.findAll()
                .stream()
                .map(this::toSubscriptionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponseDto> getAllInvoicesForAdmin(AuthenticatedUser user) {
        validateAdmin(user);
        return invoiceRepository.findAll()
                .stream()
                .map(this::toInvoiceResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminSubscriptionSummaryResponseDto getAdminSubscriptionSummary(AuthenticatedUser user) {
        validateAdmin(user);
        List<Subscription> allSubscriptions = subscriptionRepository.findAll();
        List<Invoice> allInvoices = invoiceRepository.findAll();

        long activeSubscriptions = allSubscriptions.stream()
                .filter(subscription -> Boolean.TRUE.equals(subscription.getActive()))
                .count();

        long cancelledSubscriptions = allSubscriptions.stream()
                .filter(subscription -> subscription.getStatus() == SubscriptionStatus.CANCELLED)
                .count();

        BigDecimal totalInvoicedAmount = allInvoices.stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AdminSubscriptionSummaryResponseDto.builder()
                .totalSubscriptions(allSubscriptions.size())
                .activeSubscriptions(activeSubscriptions)
                .cancelledSubscriptions(cancelledSubscriptions)
                .totalInvoices(allInvoices.size())
                .totalInvoicedAmount(totalInvoicedAmount)
                .build();
    }

    private void createInvoice(Subscription subscription, BigDecimal amount,
                               com.hireconnect.subscriptionservice.enums.PaymentMode paymentMode,
                               String transactionId) {
        Invoice invoice = Invoice.builder()
                .subscriptionId(subscription.getId())
                .recruiterId(subscription.getRecruiterId())
                .amount(amount)
                .paymentMode(paymentMode)
                .transactionId(transactionId)
                .build();
        invoiceRepository.save(invoice);
    }

    private BigDecimal getPlanAmount(PlanTier plan) {
        return switch (plan) {
            case FREE -> BigDecimal.ZERO;
            case PROFESSIONAL -> BigDecimal.valueOf(2999);
            case ENTERPRISE -> BigDecimal.valueOf(9999);
        };
    }

    private int getPlanDurationDays(PlanTier plan) {
        return plan == PlanTier.FREE ? 30 : 30;
    }

    private void validateSubscriptionPaymentInput(PlanTier plan, com.hireconnect.subscriptionservice.enums.PaymentMode paymentMode,
                                                  String transactionId) {
        if (plan == PlanTier.FREE) {
            return;
        }
        if (paymentMode == null) {
            throw new BadRequestException("Payment mode is required for paid plans");
        }
        if (transactionId == null || transactionId.isBlank()) {
            throw new BadRequestException("Transaction ID is required for paid plans");
        }
    }

    private com.hireconnect.subscriptionservice.enums.PaymentMode resolvePaymentModeForInvoice(
            PlanTier plan,
            com.hireconnect.subscriptionservice.enums.PaymentMode paymentMode
    ) {
        if (plan == PlanTier.FREE) {
            return com.hireconnect.subscriptionservice.enums.PaymentMode.WALLET;
        }
        return paymentMode;
    }

    private void validateRecruiter(AuthenticatedUser user) {
        if (user == null || user.role() != Role.RECRUITER) {
            throw new UnauthorizedException("Only recruiters can perform this action");
        }
    }

    private void validateAdmin(AuthenticatedUser user) {
        if (user == null || user.role() != Role.ADMIN) {
            throw new UnauthorizedException("Only admins can perform this action");
        }
    }

    private SubscriptionResponseDto toSubscriptionResponse(Subscription s) {
        return SubscriptionResponseDto.builder()
                .id(s.getId())
                .recruiterId(s.getRecruiterId())
                .plan(s.getPlan())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .status(s.getStatus())
                .amountPaid(s.getAmountPaid())
                .active(s.getActive())
                .build();
    }

    private InvoiceResponseDto toInvoiceResponse(Invoice i) {
        return InvoiceResponseDto.builder()
                .id(i.getId())
                .subscriptionId(i.getSubscriptionId())
                .recruiterId(i.getRecruiterId())
                .amount(i.getAmount())
                .paymentMode(i.getPaymentMode())
                .transactionId(i.getTransactionId())
                .paymentDate(i.getPaymentDate())
                .build();
    }
}
