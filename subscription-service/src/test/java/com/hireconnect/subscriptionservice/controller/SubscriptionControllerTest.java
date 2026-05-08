package com.hireconnect.subscriptionservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.subscriptionservice.dto.request.RenewSubscriptionRequestDto;
import com.hireconnect.subscriptionservice.dto.request.SubscribeRequestDto;
import com.hireconnect.subscriptionservice.dto.response.AdminSubscriptionSummaryResponseDto;
import com.hireconnect.subscriptionservice.dto.response.InvoiceResponseDto;
import com.hireconnect.subscriptionservice.dto.response.SubscriptionResponseDto;
import com.hireconnect.subscriptionservice.enums.PaymentMode;
import com.hireconnect.subscriptionservice.enums.PlanTier;
import com.hireconnect.subscriptionservice.enums.Role;
import com.hireconnect.subscriptionservice.enums.SubscriptionStatus;
import com.hireconnect.subscriptionservice.security.AuthenticatedUser;
import com.hireconnect.subscriptionservice.service.SubscriptionService;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionController subscriptionController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private AuthenticatedUser mockUser;
    
    private SubscriptionResponseDto subscriptionResponseDto;
    private InvoiceResponseDto invoiceResponseDto;

    @BeforeEach
    void setUp() {
        mockUser = new AuthenticatedUser(1L, "recruiter@test.com", Role.RECRUITER);

        mockMvc = MockMvcBuilders.standaloneSetup(subscriptionController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterAnnotation(AuthenticationPrincipal.class) != null
                                && parameter.getParameterType().equals(AuthenticatedUser.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return mockUser;
                    }
                })
                .build();
                
        subscriptionResponseDto = SubscriptionResponseDto.builder()
                .id(100L)
                .recruiterId(1L)
                .plan(PlanTier.PROFESSIONAL)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(SubscriptionStatus.ACTIVE)
                .amountPaid(BigDecimal.valueOf(2999))
                .active(true)
                .build();
                
        invoiceResponseDto = InvoiceResponseDto.builder()
                .id(200L)
                .subscriptionId(100L)
                .recruiterId(1L)
                .amount(BigDecimal.valueOf(2999))
                .paymentMode(PaymentMode.CARD)
                .transactionId("txn_123")
                .build();
    }

    @Test
    void testSubscribe() throws Exception {
        SubscribeRequestDto requestDto = new SubscribeRequestDto();
        requestDto.setPlan(PlanTier.PROFESSIONAL);
        requestDto.setPaymentMode(PaymentMode.CARD);
        requestDto.setTransactionId("txn_123");

        when(subscriptionService.subscribe(eq(mockUser), any(SubscribeRequestDto.class))).thenReturn(subscriptionResponseDto);

        mockMvc.perform(post("/api/subscriptions/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.plan").value("PROFESSIONAL"));
    }

    @Test
    void testCancel() throws Exception {
        SubscriptionResponseDto cancelledResponse = SubscriptionResponseDto.builder()
                .id(100L)
                .status(SubscriptionStatus.CANCELLED)
                .active(false)
                .build();

        when(subscriptionService.cancelSubscription(eq(mockUser))).thenReturn(cancelledResponse);

        mockMvc.perform(put("/api/subscriptions/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void testRenew() throws Exception {
        RenewSubscriptionRequestDto requestDto = new RenewSubscriptionRequestDto();
        requestDto.setPaymentMode(PaymentMode.UPI);
        requestDto.setTransactionId("txn_renew");

        when(subscriptionService.renewSubscription(eq(mockUser), any(RenewSubscriptionRequestDto.class)))
                .thenReturn(subscriptionResponseDto);

        mockMvc.perform(put("/api/subscriptions/renew")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testGetMySubscription() throws Exception {
        when(subscriptionService.getMyActiveSubscription(eq(mockUser))).thenReturn(subscriptionResponseDto);

        mockMvc.perform(get("/api/subscriptions/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testGetMyInvoices() throws Exception {
        when(subscriptionService.getMyInvoices(eq(mockUser))).thenReturn(List.of(invoiceResponseDto));

        mockMvc.perform(get("/api/subscriptions/invoices/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(200L));
    }

    @Test
    void testGetAllSubscriptionsForAdmin() throws Exception {
        when(subscriptionService.getAllSubscriptionsForAdmin(eq(mockUser))).thenReturn(List.of(subscriptionResponseDto));

        mockMvc.perform(get("/api/subscriptions/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void testGetAllInvoicesForAdmin() throws Exception {
        when(subscriptionService.getAllInvoicesForAdmin(eq(mockUser))).thenReturn(List.of(invoiceResponseDto));

        mockMvc.perform(get("/api/subscriptions/admin/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(200L));
    }

    @Test
    void testGetAdminSubscriptionSummary() throws Exception {
        AdminSubscriptionSummaryResponseDto summaryDto = AdminSubscriptionSummaryResponseDto.builder()
                .totalSubscriptions(10)
                .activeSubscriptions(5)
                .cancelledSubscriptions(5)
                .totalInvoices(10)
                .totalInvoicedAmount(BigDecimal.valueOf(10000))
                .build();

        when(subscriptionService.getAdminSubscriptionSummary(eq(mockUser))).thenReturn(summaryDto);

        mockMvc.perform(get("/api/subscriptions/admin/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSubscriptions").value(10))
                .andExpect(jsonPath("$.activeSubscriptions").value(5));
    }
}
