package com.crm.controller;

import com.crm.dto.response.ApiResponse;
import com.crm.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/subscription/webhook")
@RequiredArgsConstructor
public class RazorpayWebhookController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/razorpay")
    public ResponseEntity<ApiResponse<String>> handleRazorpayWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        log.info("Received Razorpay webhook callback (Signature present: {})", signature != null);
        subscriptionService.processWebhook(rawPayload, signature);
        return ResponseEntity.ok(ApiResponse.ok("Webhook processed successfully", "OK"));
    }
}
