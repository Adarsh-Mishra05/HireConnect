package com.hireconnect.subscriptionservice.service;

import java.util.List;

import com.hireconnect.subscriptionservice.dto.request.RenewSubscriptionRequestDto;
import com.hireconnect.subscriptionservice.dto.request.SubscribeRequestDto;
import com.hireconnect.subscriptionservice.dto.response.AdminSubscriptionSummaryResponseDto;
import com.hireconnect.subscriptionservice.dto.response.InvoiceResponseDto;
import com.hireconnect.subscriptionservice.dto.response.SubscriptionResponseDto;
import com.hireconnect.subscriptionservice.security.AuthenticatedUser;

public interface SubscriptionService {

    SubscriptionResponseDto subscribe(AuthenticatedUser user, SubscribeRequestDto requestDto);

    SubscriptionResponseDto cancelSubscription(AuthenticatedUser user);

    SubscriptionResponseDto renewSubscription(AuthenticatedUser user, RenewSubscriptionRequestDto requestDto);

    SubscriptionResponseDto getMyActiveSubscription(AuthenticatedUser user);

    List<InvoiceResponseDto> getMyInvoices(AuthenticatedUser user);

    List<SubscriptionResponseDto> getAllSubscriptionsForAdmin(AuthenticatedUser user);

    List<InvoiceResponseDto> getAllInvoicesForAdmin(AuthenticatedUser user);

    AdminSubscriptionSummaryResponseDto getAdminSubscriptionSummary(AuthenticatedUser user);
}
