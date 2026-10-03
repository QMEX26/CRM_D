package com.crm.service;

import com.crm.dto.request.CreateSubscriptionOrderRequest;
import com.crm.dto.request.VerifyPaymentRequest;
import com.crm.dto.response.RazorpayOrderResponse;
import com.crm.dto.response.SubscriptionPlanResponse;
import com.crm.dto.response.SubscriptionResponse;
import com.crm.model.Subscription;
import com.crm.model.User;

import java.util.List;

public interface SubscriptionService {
    SubscriptionResponse getSubscriptionForUser(Long userId);
    List<SubscriptionPlanResponse> getActivePlans();
    SubscriptionPlanResponse getPlanForUser(Long userId);
    RazorpayOrderResponse createOrder(Long userId, CreateSubscriptionOrderRequest request);
    SubscriptionResponse verifyPayment(Long userId, VerifyPaymentRequest request);
    Subscription initializeTrialForNewUser(User user);
    SubscriptionResponse cancelSubscription(Long userId);
    void processWebhook(String payload, String signature);
}
