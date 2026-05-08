package com.hireconnect.subscriptionservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.subscriptionservice.dto.request.RenewSubscriptionRequestDto;
import com.hireconnect.subscriptionservice.dto.request.SubscribeRequestDto;
import com.hireconnect.subscriptionservice.dto.response.AdminSubscriptionSummaryResponseDto;
import com.hireconnect.subscriptionservice.dto.response.InvoiceResponseDto;
import com.hireconnect.subscriptionservice.dto.response.SubscriptionResponseDto;
import com.hireconnect.subscriptionservice.security.AuthenticatedUser;
import com.hireconnect.subscriptionservice.service.SubscriptionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<SubscriptionResponseDto> subscribe(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody SubscribeRequestDto requestDto
    ) {
        return new ResponseEntity<>(subscriptionService.subscribe(user, requestDto), HttpStatus.CREATED);
    }

    @PutMapping("/cancel")
    public ResponseEntity<SubscriptionResponseDto> cancel(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(user));
    }

    @PutMapping("/renew")
    public ResponseEntity<SubscriptionResponseDto> renew(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody RenewSubscriptionRequestDto requestDto
    ) {
        return ResponseEntity.ok(subscriptionService.renewSubscription(user, requestDto));
    }

    @GetMapping("/me")
    public ResponseEntity<SubscriptionResponseDto> getMySubscription(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(subscriptionService.getMyActiveSubscription(user));
    }

    @GetMapping("/invoices/me")
    public ResponseEntity<List<InvoiceResponseDto>> getMyInvoices(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(subscriptionService.getMyInvoices(user));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<SubscriptionResponseDto>> getAllSubscriptionsForAdmin(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptionsForAdmin(user));
    }

    @GetMapping("/admin/invoices")
    public ResponseEntity<List<InvoiceResponseDto>> getAllInvoicesForAdmin(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(subscriptionService.getAllInvoicesForAdmin(user));
    }

    @GetMapping("/admin/summary")
    public ResponseEntity<AdminSubscriptionSummaryResponseDto> getAdminSubscriptionSummary(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(subscriptionService.getAdminSubscriptionSummary(user));
    }
}
