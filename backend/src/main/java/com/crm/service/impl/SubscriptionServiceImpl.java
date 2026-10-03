package com.crm.service.impl;

import com.crm.dto.request.CreateSubscriptionOrderRequest;
import com.crm.dto.request.VerifyPaymentRequest;
import com.crm.dto.response.RazorpayOrderResponse;
import com.crm.dto.response.SubscriptionPlanResponse;
import com.crm.dto.response.SubscriptionResponse;
import com.crm.exception.BusinessException;
import com.crm.exception.ResourceNotFoundException;
import com.crm.model.*;
import com.crm.repository.SubscriptionPaymentRepository;
import com.crm.repository.SubscriptionPlanRepository;
import com.crm.repository.SubscriptionRepository;
import com.crm.repository.UserRepository;
import com.crm.service.AuditService;
import com.crm.service.RazorpayService;
import com.crm.service.SubscriptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final UserRepository userRepository;
    private final RazorpayService razorpayService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public SubscriptionResponse getSubscriptionForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Subscription subscription = subscriptionRepository.findByUserId(userId).orElse(null);

        // Auto-reconcile any pending Razorpay payments for interrupted/dropped network flows
        List<SubscriptionPayment> recentPayments = subscriptionPaymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (recentPayments != null && !recentPayments.isEmpty()) {
            for (SubscriptionPayment pending : recentPayments) {
                if (pending.getStatus() == PaymentStatus.CREATED && pending.getCreatedAt() != null &&
                        pending.getCreatedAt().isAfter(LocalDateTime.now().minusHours(24))) {
                    try {
                        Map<String, Object> rzpPayment = razorpayService.fetchOrderPayment(pending.getRazorpayOrderId());
                        if (rzpPayment != null && rzpPayment.containsKey("status")) {
                            String rzpStatus = (String) rzpPayment.get("status");
                            if ("captured".equalsIgnoreCase(rzpStatus) || "authorized".equalsIgnoreCase(rzpStatus)) {
                                String rzpPaymentId = (String) rzpPayment.get("paymentId");
                                log.info("[RAZORPAY PAYMENT STATUS]\nstatus={}", rzpStatus);

                                pending.setRazorpayPaymentId(rzpPaymentId);
                                pending.setStatus(PaymentStatus.SUCCESS);
                                pending.setPaidAt(LocalDateTime.now());
                                subscriptionPaymentRepository.save(pending);

                                LocalDateTime now = LocalDateTime.now();
                                if (subscription == null) {
                                    subscription = Subscription.builder()
                                            .user(user)
                                            .plan(pending.getPlan())
                                            .status(SubscriptionStatus.ACTIVE)
                                            .currentPeriodStart(now)
                                            .currentPeriodEnd(now.plusMonths(1))
                                            .build();
                                } else {
                                    subscription.setPlan(pending.getPlan());
                                    subscription.setStatus(SubscriptionStatus.ACTIVE);
                                    subscription.setCurrentPeriodStart(now);
                                    if (subscription.getCurrentPeriodEnd() != null && subscription.getCurrentPeriodEnd().isAfter(now)) {
                                        subscription.setCurrentPeriodEnd(subscription.getCurrentPeriodEnd().plusMonths(1));
                                    } else {
                                        subscription.setCurrentPeriodEnd(now.plusMonths(1));
                                    }
                                }
                                subscription = subscriptionRepository.save(subscription);
                                pending.setSubscription(subscription);
                                subscriptionPaymentRepository.save(pending);

                                log.info("[SUBSCRIPTION ACTIVATION]\nuserId={}\nsubscriptionId={}\nstartDate={}\nendDate={}",
                                        userId, subscription.getId(), subscription.getCurrentPeriodStart(), subscription.getCurrentPeriodEnd());
                                log.info("[REVENUE UPDATE]\npaymentId={}\namount={}", rzpPaymentId, pending.getAmount());
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Auto-reconciliation check for order {}: {}", pending.getRazorpayOrderId(), e.getMessage());
                    }
                }
            }
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. If user has an active PAID subscription, it takes strict priority over trial
        if (subscription != null && subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            if (subscription.getCurrentPeriodEnd() != null && subscription.getCurrentPeriodEnd().isAfter(now)) {
                return mapSubscriptionToResponse(subscription);
            } else {
                // Paid subscription expired
                subscription.setStatus(SubscriptionStatus.EXPIRED);
                subscription = subscriptionRepository.save(subscription);
                return mapSubscriptionToResponse(subscription);
            }
        }

        // 2. If subscription is explicitly CANCELLED, honor the cancellation
        if (subscription != null && subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            return mapSubscriptionToResponse(subscription);
        }

        // 3. Free Trial calculation based on user.created_at (Server Time)
        // Formula: trialStart = user.created_at, trialEnd = user.created_at + 7 days
        LocalDateTime createdAt = user.getCreatedAt() != null ? user.getCreatedAt() : now;
        LocalDateTime trialStart = createdAt;
        LocalDateTime trialEnd = createdAt.plusDays(7);
        boolean isTrialValid = now.isBefore(trialEnd);

        if (isTrialValid) {
            long secondsRemaining = ChronoUnit.SECONDS.between(now, trialEnd);
            long daysRemaining = (secondsRemaining + 86399) / 86400; // Ceiling to full remaining days
            daysRemaining = Math.max(1, Math.min(7, daysRemaining));

            if (subscription == null) {
                SubscriptionPlan plan = resolvePlanForUser(user);
                subscription = Subscription.builder()
                        .user(user)
                        .plan(plan)
                        .status(SubscriptionStatus.FREE_TRIAL)
                        .trialStartAt(trialStart)
                        .trialEndAt(trialEnd)
                        .build();
                subscription = subscriptionRepository.save(subscription);
            } else if (subscription.getStatus() != SubscriptionStatus.FREE_TRIAL) {
                subscription.setStatus(SubscriptionStatus.FREE_TRIAL);
                subscription.setTrialStartAt(trialStart);
                subscription.setTrialEndAt(trialEnd);
                subscription = subscriptionRepository.save(subscription);
            }
            return mapSubscriptionToResponse(subscription);
        } else {
            // Free trial has expired
            if (subscription == null) {
                SubscriptionPlan plan = resolvePlanForUser(user);
                subscription = Subscription.builder()
                        .user(user)
                        .plan(plan)
                        .status(SubscriptionStatus.EXPIRED)
                        .trialStartAt(trialStart)
                        .trialEndAt(trialEnd)
                        .build();
                subscription = subscriptionRepository.save(subscription);
            } else if (subscription.getStatus() != SubscriptionStatus.EXPIRED) {
                subscription.setStatus(SubscriptionStatus.EXPIRED);
                if (subscription.getTrialStartAt() == null) subscription.setTrialStartAt(trialStart);
                if (subscription.getTrialEndAt() == null) subscription.setTrialEndAt(trialEnd);
                subscription = subscriptionRepository.save(subscription);
            }
            return mapSubscriptionToResponse(subscription);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getActivePlans() {
        return subscriptionPlanRepository.findByActiveTrue().stream()
                .map(this::mapPlanToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getPlanForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        SubscriptionPlan plan = resolvePlanForUser(user);
        return mapPlanToResponse(plan);
    }

    @Override
    @Transactional
    public RazorpayOrderResponse createOrder(Long userId, CreateSubscriptionOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Security: Plan is determined strictly on backend based on user's role
        SubscriptionPlan plan = resolvePlanForUser(user);

        // Amount in paise (1 INR = 100 paise)
        long amountInPaise = plan.getPrice().multiply(BigDecimal.valueOf(100)).longValue();
        String receipt = "rcpt_sub_" + user.getId() + "_" + System.currentTimeMillis();

        Map<String, String> notes = new HashMap<>();
        notes.put("userId", String.valueOf(user.getId()));
        notes.put("userEmail", user.getEmail());
        notes.put("planName", plan.getName());
        notes.put("planPrice", plan.getPrice().toPlainString());

        String orderId = razorpayService.createOrder(amountInPaise, plan.getCurrency(), receipt, notes);

        log.info("[RAZORPAY ORDER CREATED]\nuserId={}\norderId={}\namount={}",
                userId, orderId, plan.getPrice());

        Subscription subscription = subscriptionRepository.findByUserId(userId).orElse(null);

        SubscriptionPayment payment = SubscriptionPayment.builder()
                .user(user)
                .subscription(subscription)
                .plan(plan)
                .razorpayOrderId(orderId)
                .amount(plan.getPrice())
                .amountInPaise(amountInPaise)
                .currency(plan.getCurrency())
                .status(PaymentStatus.CREATED)
                .build();

        subscriptionPaymentRepository.save(payment);

        auditService.logAction(userId, "Subscription", user.getId(), "CREATE_PAYMENT_ORDER", null,
                "Plan: " + plan.getName() + ", Amount: ₹" + plan.getPrice() + ", Razorpay OrderId: " + orderId);

        return RazorpayOrderResponse.builder()
                .orderId(orderId)
                .amountInPaise(amountInPaise)
                .amount(plan.getPrice())
                .currency(plan.getCurrency())
                .keyId(razorpayService.getKeyId())
                .planName(plan.getName())
                .userEmail(user.getEmail())
                .userName(user.getName())
                .userPhone(user.getPhone())
                .build();
    }

    @Override
    @Transactional
    public SubscriptionResponse verifyPayment(Long userId, VerifyPaymentRequest request) {
        log.info("[RAZORPAY PAYMENT CALLBACK]\nuserId={}\norderId={}\npaymentId={}",
                userId, request.getRazorpayOrderId(), request.getRazorpayPaymentId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        SubscriptionPayment payment = subscriptionPaymentRepository.findFirstByRazorpayOrderIdOrderByCreatedAtDesc(request.getRazorpayOrderId())
                .orElseThrow(() -> {
                    log.error("[PAYMENT ERROR]\nPayment record not found for order: {}", request.getRazorpayOrderId());
                    return new ResourceNotFoundException("Payment record not found for order: " + request.getRazorpayOrderId());
                });

        if (!payment.getUser().getId().equals(userId)) {
            log.error("[PAYMENT ERROR]\nOrder userId mismatch. Expected: {}, Actual: {}", payment.getUser().getId(), userId);
            throw new BusinessException("Payment order does not belong to the authenticated user");
        }

        // Idempotency: If this payment was already verified successfully, return active subscription without duplicate revenue
        if (payment.getStatus() == PaymentStatus.SUCCESS &&
            request.getRazorpayPaymentId() != null &&
            request.getRazorpayPaymentId().equals(payment.getRazorpayPaymentId())) {
            log.info("Payment order {} already verified with payment ID {}. Idempotent return.",
                    request.getRazorpayOrderId(), request.getRazorpayPaymentId());
            Subscription existingSub = subscriptionRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subscription not found for user: " + userId));
            return mapSubscriptionToResponse(existingSub);
        }

        // Signature Verification using HMAC-SHA256
        boolean isValid = razorpayService.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        log.info("[RAZORPAY SIGNATURE VERIFICATION]\nresult={}", isValid ? "SUCCESS" : "FAILED");

        // If signature fails, check Razorpay direct API as safety fallback before rejecting
        if (!isValid) {
            Map<String, Object> rzpPayment = razorpayService.fetchOrderPayment(request.getRazorpayOrderId());
            if (rzpPayment != null && ("captured".equalsIgnoreCase((String) rzpPayment.get("status")) || "authorized".equalsIgnoreCase((String) rzpPayment.get("status")))) {
                log.info("[RAZORPAY PAYMENT STATUS]\nstatus={} (Verified via Razorpay API)", rzpPayment.get("status"));
                isValid = true;
            }
        }

        if (!isValid) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Invalid Razorpay payment signature");
            subscriptionPaymentRepository.save(payment);
            log.error("[PAYMENT ERROR]\nPayment verification failed: Signature mismatch for order: {}", request.getRazorpayOrderId());
            throw new BusinessException("Payment verification failed: Invalid signature");
        }

        log.info("[RAZORPAY PAYMENT STATUS]\nstatus=SUCCESS/CAPTURED");

        LocalDateTime now = LocalDateTime.now();

        // Update payment transaction record
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(now);
        subscriptionPaymentRepository.save(payment);

        // Find or create subscription for user
        Subscription subscription = subscriptionRepository.findByUserId(userId).orElse(null);
        if (subscription == null) {
            subscription = Subscription.builder()
                    .user(user)
                    .plan(payment.getPlan())
                    .status(SubscriptionStatus.ACTIVE)
                    .currentPeriodStart(now)
                    .currentPeriodEnd(now.plusMonths(1))
                    .build();
        } else {
            subscription.setPlan(payment.getPlan());
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setCurrentPeriodStart(now);
            // If already active with remaining days, extend from currentPeriodEnd, otherwise from now
            if (subscription.getCurrentPeriodEnd() != null && subscription.getCurrentPeriodEnd().isAfter(now)) {
                subscription.setCurrentPeriodEnd(subscription.getCurrentPeriodEnd().plusMonths(1));
            } else {
                subscription.setCurrentPeriodEnd(now.plusMonths(1));
            }
        }

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        payment.setSubscription(savedSubscription);
        subscriptionPaymentRepository.save(payment);

        log.info("[SUBSCRIPTION ACTIVATION]\nuserId={}\nsubscriptionId={}\nstartDate={}\nendDate={}",
                userId, savedSubscription.getId(), savedSubscription.getCurrentPeriodStart(), savedSubscription.getCurrentPeriodEnd());
        log.info("[REVENUE UPDATE]\npaymentId={}\namount={}",
                request.getRazorpayPaymentId(), payment.getAmount());

        auditService.logAction(userId, "Subscription", savedSubscription.getId(), "ACTIVATE_SUBSCRIPTION", null,
                "Plan: " + savedSubscription.getPlan().getName() + ", PaymentId: " + request.getRazorpayPaymentId() +
                ", Valid Until: " + savedSubscription.getCurrentPeriodEnd());

        return mapSubscriptionToResponse(savedSubscription);
    }

    @Override
    @Transactional
    public Subscription initializeTrialForNewUser(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }

        if (subscriptionRepository.existsByUserId(user.getId())) {
            return subscriptionRepository.findByUserId(user.getId()).orElse(null);
        }

        SubscriptionPlan plan = resolvePlanForUser(user);
        LocalDateTime trialStart = user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now();
        LocalDateTime trialEnd = trialStart.plusDays(7);

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .status(SubscriptionStatus.FREE_TRIAL)
                .trialStartAt(trialStart)
                .trialEndAt(trialEnd)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Initialized 7-day FREE_TRIAL subscription for new user: {} (Trial Start: {}, Trial End: {})",
                user.getEmail(), trialStart, trialEnd);

        return saved;
    }

    @Override
    @Transactional
    public SubscriptionResponse cancelSubscription(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found to cancel"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        Subscription saved = subscriptionRepository.save(subscription);

        auditService.logAction(userId, "Subscription", saved.getId(), "CANCEL_SUBSCRIPTION", null,
                "Subscription cancelled by user");

        return mapSubscriptionToResponse(saved);
    }

    @Override
    @Transactional
    public void processWebhook(String payload, String signature) {
        boolean isValid = razorpayService.verifyWebhookSignature(payload, signature);
        if (!isValid) {
            log.warn("Invalid Razorpay webhook signature. Request discarded.");
            throw new BusinessException("Invalid webhook signature");
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String event = root.has("event") ? root.get("event").asText() : "";
            log.info("Received valid Razorpay webhook event: {}", event);

            if ("payment.captured".equalsIgnoreCase(event) || "order.paid".equalsIgnoreCase(event)) {
                JsonNode paymentNode = root.path("payload").path("payment").path("entity");
                String orderId = paymentNode.path("order_id").asText(null);
                String paymentId = paymentNode.path("id").asText(null);

                if (orderId != null && paymentId != null) {
                    subscriptionPaymentRepository.findFirstByRazorpayOrderIdOrderByCreatedAtDesc(orderId).ifPresent(payment -> {
                        if (payment.getStatus() != PaymentStatus.SUCCESS) {
                            payment.setRazorpayPaymentId(paymentId);
                            payment.setStatus(PaymentStatus.SUCCESS);
                            payment.setPaidAt(LocalDateTime.now());
                            subscriptionPaymentRepository.save(payment);

                            Subscription subscription = payment.getSubscription();
                            if (subscription != null) {
                                subscription.setStatus(SubscriptionStatus.ACTIVE);
                                if (subscription.getCurrentPeriodEnd() == null || subscription.getCurrentPeriodEnd().isBefore(LocalDateTime.now())) {
                                    subscription.setCurrentPeriodStart(LocalDateTime.now());
                                    subscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
                                }
                                subscriptionRepository.save(subscription);
                            }
                        }
                    });
                }
            } else if ("payment.failed".equalsIgnoreCase(event)) {
                JsonNode paymentNode = root.path("payload").path("payment").path("entity");
                String orderId = paymentNode.path("order_id").asText(null);
                String errorDescription = paymentNode.path("error_description").asText("Payment failed");

                if (orderId != null) {
                    subscriptionPaymentRepository.findFirstByRazorpayOrderIdOrderByCreatedAtDesc(orderId).ifPresent(payment -> {
                        if (payment.getStatus() != PaymentStatus.SUCCESS) {
                            payment.setStatus(PaymentStatus.FAILED);
                            payment.setFailureReason(errorDescription);
                            subscriptionPaymentRepository.save(payment);
                        }
                    });
                }
            }
        } catch (Exception e) {
            log.error("Error processing Razorpay webhook payload: {}", e.getMessage(), e);
            throw new BusinessException("Error processing webhook payload: " + e.getMessage());
        }
    }

    private SubscriptionPlan resolvePlanForUser(User user) {
        String roleName = user.getRole() != null ? user.getRole().getName() : "ROLE_USER";
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(roleName);

        String targetPlanName = isAdmin ? "ADMIN_MONTHLY" : "USER_MONTHLY";
        String targetRole = isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
        BigDecimal defaultPrice = isAdmin ? new BigDecimal("299.00") : new BigDecimal("99.00");

        return subscriptionPlanRepository.findByName(targetPlanName)
                .orElseGet(() -> subscriptionPlanRepository.save(
                        SubscriptionPlan.builder()
                                .name(targetPlanName)
                                .targetRole(targetRole)
                                .price(defaultPrice)
                                .currency("INR")
                                .billingCycle("MONTHLY")
                                .trialDays(7)
                                .active(true)
                                .build()
                ));
    }

    private SubscriptionResponse mapSubscriptionToResponse(Subscription subscription) {
        User user = subscription.getUser();
        LocalDateTime now = LocalDateTime.now();

        long daysRemaining = 0;
        boolean isTrial = subscription.getStatus() == SubscriptionStatus.FREE_TRIAL;
        boolean isActive = subscription.getStatus() == SubscriptionStatus.ACTIVE;
        boolean isExpired = subscription.getStatus() == SubscriptionStatus.EXPIRED;
        boolean isCancelled = subscription.getStatus() == SubscriptionStatus.CANCELLED;

        LocalDateTime trialStart = subscription.getTrialStartAt() != null
                ? subscription.getTrialStartAt()
                : (user.getCreatedAt() != null ? user.getCreatedAt() : now);
        LocalDateTime trialEnd = subscription.getTrialEndAt() != null
                ? subscription.getTrialEndAt()
                : trialStart.plusDays(7);

        if (isTrial) {
            if (trialEnd.isAfter(now)) {
                long seconds = ChronoUnit.SECONDS.between(now, trialEnd);
                daysRemaining = (seconds + 86399) / 86400;
                daysRemaining = Math.max(1, Math.min(7, daysRemaining));
            } else {
                daysRemaining = 0;
                isTrial = false;
                isExpired = true;
            }
        } else if (isActive && subscription.getCurrentPeriodEnd() != null) {
            if (subscription.getCurrentPeriodEnd().isAfter(now)) {
                long seconds = ChronoUnit.SECONDS.between(now, subscription.getCurrentPeriodEnd());
                daysRemaining = (seconds + 86399) / 86400;
                if (daysRemaining <= 0) daysRemaining = 1;
            } else {
                daysRemaining = 0;
                isActive = false;
                isExpired = true;
            }
        }

        String statusStr = isTrial ? "FREE_TRIAL" : (isActive ? "ACTIVE" : (isCancelled ? "CANCELLED" : "EXPIRED"));
        String subscriptionType = isTrial ? "FREE_TRIAL" : (isActive ? "PAID" : (isCancelled ? "CANCELLED" : "EXPIRED"));

        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .userRole(user.getRole() != null ? user.getRole().getName() : "ROLE_USER")
                .plan(mapPlanToResponse(subscription.getPlan()))
                .status(statusStr)
                .subscriptionType(subscriptionType)
                .isTrialActive(isTrial)
                .trialStartDate(trialStart)
                .trialEndDate(trialEnd)
                .trialDaysRemaining(isTrial ? daysRemaining : 0L)
                .trialStartAt(trialStart)
                .trialEndAt(trialEnd)
                .currentPeriodStart(subscription.getCurrentPeriodStart())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .daysRemaining(daysRemaining)
                .isTrial(isTrial)
                .isActive(isActive || isTrial)
                .isExpired(isExpired)
                .build();
    }

    private SubscriptionPlanResponse mapPlanToResponse(SubscriptionPlan plan) {
        if (plan == null) return null;
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .targetRole(plan.getTargetRole())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .billingCycle(plan.getBillingCycle())
                .trialDays(plan.getTrialDays())
                .active(plan.getActive())
                .build();
    }
}
