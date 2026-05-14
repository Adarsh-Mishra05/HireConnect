package com.hireconnect.subscriptionservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hireconnect.subscriptionservice.dto.request.RenewSubscriptionRequestDto;
import com.hireconnect.subscriptionservice.dto.request.SubscribeRequestDto;
import com.hireconnect.subscriptionservice.dto.response.AdminSubscriptionSummaryResponseDto;
import com.hireconnect.subscriptionservice.dto.response.InvoiceResponseDto;
import com.hireconnect.subscriptionservice.dto.response.SubscriptionResponseDto;
import com.hireconnect.subscriptionservice.entity.Invoice;
import com.hireconnect.subscriptionservice.entity.Subscription;
import com.hireconnect.subscriptionservice.enums.PaymentMode;
import com.hireconnect.subscriptionservice.enums.PlanTier;
import com.hireconnect.subscriptionservice.enums.Role;
import com.hireconnect.subscriptionservice.enums.SubscriptionStatus;
import com.hireconnect.subscriptionservice.exception.BadRequestException;
import com.hireconnect.subscriptionservice.exception.ResourceNotFoundException;
import com.hireconnect.subscriptionservice.exception.UnauthorizedException;
import com.hireconnect.subscriptionservice.repository.InvoiceRepository;
import com.hireconnect.subscriptionservice.repository.SubscriptionRepository;
import com.hireconnect.subscriptionservice.security.AuthenticatedUser;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private AuthenticatedUser recruiterUser;
    private AuthenticatedUser adminUser;
    private Subscription activeSubscription;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        recruiterUser = new AuthenticatedUser(1L, "recruiter@test.com", Role.RECRUITER);
        adminUser = new AuthenticatedUser(2L, "admin@test.com", Role.ADMIN);

        activeSubscription = Subscription.builder()
                .id(100L)
                .recruiterId(1L)
                .plan(PlanTier.PROFESSIONAL)
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(20))
                .status(SubscriptionStatus.ACTIVE)
                .amountPaid(BigDecimal.valueOf(2999))
                .active(true)
                .build();

        invoice = Invoice.builder()
                .id(200L)
                .subscriptionId(100L)
                .recruiterId(1L)
                .amount(BigDecimal.valueOf(2999))
                .paymentMode(PaymentMode.CARD)
                .transactionId("txn_123")
                .paymentDate(LocalDate.now().atStartOfDay())
                .build();
    }

    // --- subscribe ---
    @Test
    void testSubscribe_Success() {
        SubscribeRequestDto requestDto = new SubscribeRequestDto();
        requestDto.setPlan(PlanTier.PROFESSIONAL);
        requestDto.setPaymentMode(PaymentMode.CARD);
        requestDto.setTransactionId("txn_new");

        when(subscriptionRepository.findFirstByRecruiterIdAndActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription s = invocation.getArgument(0);
            s.setId(101L);
            return s;
        });

        SubscriptionResponseDto response = subscriptionService.subscribe(recruiterUser, requestDto);

        assertNotNull(response);
        assertEquals(PlanTier.PROFESSIONAL, response.getPlan());
        assertTrue(response.getActive());
        assertEquals(SubscriptionStatus.ACTIVE, response.getStatus());

        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void testSubscribe_AlreadyActive() {
        SubscribeRequestDto requestDto = new SubscribeRequestDto();
        requestDto.setPlan(PlanTier.PROFESSIONAL);
        requestDto.setPaymentMode(PaymentMode.CARD);
        requestDto.setTransactionId("txn_existing");

        when(subscriptionRepository.findFirstByRecruiterIdAndActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(activeSubscription));

        assertThrows(BadRequestException.class, () -> subscriptionService.subscribe(recruiterUser, requestDto));
    }

    @Test
    void testSubscribe_NotRecruiter() {
        assertThrows(UnauthorizedException.class, () -> subscriptionService.subscribe(adminUser, new SubscribeRequestDto()));
    }

    // --- cancelSubscription ---
    @Test
    void testCancelSubscription_Success() {
        when(subscriptionRepository.findFirstByRecruiterIdAndActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        SubscriptionResponseDto response = subscriptionService.cancelSubscription(recruiterUser);

        assertNotNull(response);
        assertFalse(response.getActive());
        assertEquals(SubscriptionStatus.CANCELLED, response.getStatus());
    }

    @Test
    void testCancelSubscription_NotFound() {
        when(subscriptionRepository.findFirstByRecruiterIdAndActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subscriptionService.cancelSubscription(recruiterUser));
    }

    // --- renewSubscription ---
    @Test
    void testRenewSubscription_Success() {
        RenewSubscriptionRequestDto requestDto = new RenewSubscriptionRequestDto();
        requestDto.setPaymentMode(PaymentMode.UPI);
        requestDto.setTransactionId("txn_renew");

        when(subscriptionRepository.findFirstByRecruiterIdAndActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        SubscriptionResponseDto response = subscriptionService.renewSubscription(recruiterUser, requestDto);

        assertNotNull(response);
        assertTrue(response.getActive());
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void testRenewSubscription_NotFound() {
        RenewSubscriptionRequestDto requestDto = new RenewSubscriptionRequestDto();

        when(subscriptionRepository.findFirstByRecruiterIdAndActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subscriptionService.renewSubscription(recruiterUser, requestDto));
    }

    // --- getMyActiveSubscription ---
    @Test
    void testGetMyActiveSubscription_Success() {
        when(subscriptionRepository.findFirstByRecruiterIdAndActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(activeSubscription));

        SubscriptionResponseDto response = subscriptionService.getMyActiveSubscription(recruiterUser);

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    void testGetMyActiveSubscription_NotFound() {
        when(subscriptionRepository.findFirstByRecruiterIdAndActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subscriptionService.getMyActiveSubscription(recruiterUser));
    }

    // --- getMyInvoices ---
    @Test
    void testGetMyInvoices_Success() {
        when(invoiceRepository.findByRecruiterIdOrderByPaymentDateDesc(1L))
                .thenReturn(List.of(invoice));

        List<InvoiceResponseDto> response = subscriptionService.getMyInvoices(recruiterUser);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(200L, response.get(0).getId());
    }

    // --- Admin methods ---
    @Test
    void testGetAllSubscriptionsForAdmin_Success() {
        when(subscriptionRepository.findAll()).thenReturn(List.of(activeSubscription));

        List<SubscriptionResponseDto> response = subscriptionService.getAllSubscriptionsForAdmin(adminUser);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void testGetAllSubscriptionsForAdmin_NotAdmin() {
        assertThrows(UnauthorizedException.class, () -> subscriptionService.getAllSubscriptionsForAdmin(recruiterUser));
    }

    @Test
    void testGetAllInvoicesForAdmin_Success() {
        when(invoiceRepository.findAll()).thenReturn(List.of(invoice));

        List<InvoiceResponseDto> response = subscriptionService.getAllInvoicesForAdmin(adminUser);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void testGetAdminSubscriptionSummary_Success() {
        Subscription cancelledSubscription = Subscription.builder()
                .status(SubscriptionStatus.CANCELLED)
                .active(false)
                .build();

        when(subscriptionRepository.findAll()).thenReturn(List.of(activeSubscription, cancelledSubscription));
        when(invoiceRepository.findAll()).thenReturn(List.of(invoice));

        AdminSubscriptionSummaryResponseDto response = subscriptionService.getAdminSubscriptionSummary(adminUser);

        assertNotNull(response);
        assertEquals(2, response.getTotalSubscriptions());
        assertEquals(1, response.getActiveSubscriptions());
        assertEquals(1, response.getCancelledSubscriptions());
        assertEquals(1, response.getTotalInvoices());
        assertEquals(BigDecimal.valueOf(2999), response.getTotalInvoicedAmount());
    }
    
    @Test
    void testGetPlanAmountFree() {
        SubscribeRequestDto requestDto = new SubscribeRequestDto();
        requestDto.setPlan(PlanTier.FREE);
        requestDto.setPaymentMode(PaymentMode.CARD);
        requestDto.setTransactionId("txn_free");

        when(subscriptionRepository.findFirstByRecruiterIdAndActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription s = invocation.getArgument(0);
            s.setId(102L);
            return s;
        });

        SubscriptionResponseDto response = subscriptionService.subscribe(recruiterUser, requestDto);

        assertNotNull(response);
        assertEquals(PlanTier.FREE, response.getPlan());
        assertEquals(BigDecimal.ZERO, response.getAmountPaid());
    }
    
    @Test
    void testGetPlanAmountEnterprise() {
        SubscribeRequestDto requestDto = new SubscribeRequestDto();
        requestDto.setPlan(PlanTier.ENTERPRISE);
        requestDto.setPaymentMode(PaymentMode.CARD);
        requestDto.setTransactionId("txn_enterprise");

        when(subscriptionRepository.findFirstByRecruiterIdAndActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription s = invocation.getArgument(0);
            s.setId(103L);
            return s;
        });

        SubscriptionResponseDto response = subscriptionService.subscribe(recruiterUser, requestDto);

        assertNotNull(response);
        assertEquals(PlanTier.ENTERPRISE, response.getPlan());
        assertEquals(BigDecimal.valueOf(9999), response.getAmountPaid());
    }
}
