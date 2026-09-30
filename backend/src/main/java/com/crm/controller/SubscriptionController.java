package com.crm.controller;

import com.crm.dto.request.CreateSubscriptionOrderRequest;
import com.crm.dto.request.VerifyPaymentRequest;
import com.crm.dto.response.ApiResponse;
import com.crm.dto.response.RazorpayOrderResponse;
import com.crm.dto.response.SubscriptionPlanResponse;
import com.crm.dto.response.SubscriptionResponse;
import com.crm.security.CurrentUser;
import com.crm.security.UserPrincipal;
import com.crm.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getMySubscription(@CurrentUser UserPrincipal principal) {
        SubscriptionResponse response = subscriptionService.getSubscriptionForUser(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Subscription retrieved successfully", response));
    }

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getActivePlans() {
        List<SubscriptionPlanResponse> plans = subscriptionService.getActivePlans();
        return ResponseEntity.ok(ApiResponse.ok("Active plans retrieved successfully", plans));
    }

    @GetMapping("/plan")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getMyPlan(@CurrentUser UserPrincipal principal) {
        SubscriptionPlanResponse plan = subscriptionService.getPlanForUser(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Applicable plan retrieved successfully", plan));
    }

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createOrder(
            @CurrentUser UserPrincipal principal,
            @RequestBody(required = false) CreateSubscriptionOrderRequest request) {
        if (request == null) {
            request = new CreateSubscriptionOrderRequest();
        }
        RazorpayOrderResponse order = subscriptionService.createOrder(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Razorpay order created successfully", order));
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> verifyPayment(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody VerifyPaymentRequest request) {
        SubscriptionResponse response = subscriptionService.verifyPayment(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Payment verified and subscription activated successfully", response));
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription(@CurrentUser UserPrincipal principal) {
        SubscriptionResponse response = subscriptionService.cancelSubscription(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Subscription cancelled successfully", response));
    }
}
