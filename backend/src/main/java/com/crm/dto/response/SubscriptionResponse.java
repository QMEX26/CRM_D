package com.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userRole;
    private SubscriptionPlanResponse plan;
    private String status; // FREE_TRIAL, ACTIVE, EXPIRED, CANCELLED
    private String subscriptionType; // FREE_TRIAL, PAID, EXPIRED, CANCELLED

    // Free Trial specific fields
    private Boolean isTrialActive;
    private LocalDateTime trialStartDate;
    private LocalDateTime trialEndDate;
    private Long trialDaysRemaining;

    // Existing compatibility fields
    private LocalDateTime trialStartAt;
    private LocalDateTime trialEndAt;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private Long daysRemaining;
    private Boolean isTrial;
    private Boolean isActive;
    private Boolean isExpired;
}

