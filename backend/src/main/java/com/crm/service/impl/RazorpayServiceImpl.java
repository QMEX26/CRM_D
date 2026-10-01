package com.crm.service.impl;

import com.crm.exception.BusinessException;
import com.crm.service.RazorpayService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class RazorpayServiceImpl implements RazorpayService {

    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String RAZORPAY_ORDERS_URL = "https://api.razorpay.com/v1/orders";

    @Value("${razorpay.key.id:}")
    private String keyId;

    @Value("${razorpay.key.secret:}")
    private String keySecret;

    @Value("${razorpay.webhook.secret:}")
    private String webhookSecret;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RazorpayServiceImpl(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String createOrder(long amountInPaise, String currency, String receipt, Map<String, String> notes) {
        // If keys are not configured or placeholder, support mock order generation for safe offline testing
        if (keyId == null || keyId.isBlank() || keyId.startsWith("YOUR_") ||
            keySecret == null || keySecret.isBlank() || keySecret.startsWith("YOUR_")) {
            log.warn("Razorpay credentials not configured. Generating mock order for development/testing.");
            return "order_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(keyId.trim(), keySecret.trim());

            Map<String, Object> body = new HashMap<>();
            body.put("amount", amountInPaise);
            body.put("currency", currency != null ? currency : "INR");
            body.put("receipt", receipt);
            if (notes != null && !notes.isEmpty()) {
                body.put("notes", notes);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    RAZORPAY_ORDERS_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.has("id")) {
                    return root.get("id").asText();
                }
            }

            throw new BusinessException("Failed to obtain Order ID from Razorpay: " + response.getBody());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Razorpay order creation failed: {}", e.getMessage(), e);
            throw new BusinessException("Unable to create payment order with Razorpay: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        if (signature == null || signature.isBlank() || orderId == null || paymentId == null) {
            return false;
        }

        // Mock/Sandbox bypass: Support simulated test checkouts when using Razorpay Test Mode keys (rzp_test_...) or unconfigured keys
        if (signature.startsWith("mock_sig_") &&
            (keyId == null || keyId.isBlank() || keyId.startsWith("rzp_test_") || keyId.startsWith("YOUR_") ||
             keySecret == null || keySecret.isBlank() || keySecret.startsWith("YOUR_"))) {
            log.info("Accepting sandbox/test mode payment simulation for order: {}", orderId);
            return true;
        }

        try {
            String payload = orderId + "|" + paymentId;
            String expectedSignature = calculateHmacSha256(payload, keySecret != null ? keySecret.trim() : "");
            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.trim().getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Error verifying Razorpay payment signature: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean verifyWebhookSignature(String requestBody, String signature) {
        if (signature == null || signature.isBlank() || requestBody == null) {
            return false;
        }

        if (webhookSecret == null || webhookSecret.isBlank() || webhookSecret.startsWith("YOUR_")) {
            log.warn("RAZORPAY_WEBHOOK_SECRET is not configured. Webhook verification skipped.");
            return false;
        }

        try {
            String expectedSignature = calculateHmacSha256(requestBody, webhookSecret.trim());
            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.trim().getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Error verifying Razorpay webhook signature: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getKeyId() {
        return keyId != null ? keyId : "";
    }

    @Override
    public Map<String, Object> fetchOrderPayment(String orderId) {
        if (orderId == null || orderId.isBlank() || keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank() || keyId.startsWith("YOUR_")) {
            return null;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(keyId.trim(), keySecret.trim());
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = RAZORPAY_ORDERS_URL + "/" + orderId.trim() + "/payments";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode items = root.path("items");
                if (items.isArray() && items.size() > 0) {
                    for (JsonNode payment : items) {
                        String status = payment.path("status").asText("");
                        if ("captured".equalsIgnoreCase(status) || "authorized".equalsIgnoreCase(status)) {
                            Map<String, Object> res = new HashMap<>();
                            res.put("paymentId", payment.path("id").asText(""));
                            res.put("status", status);
                            res.put("amount", payment.path("amount").asLong(0));
                            res.put("currency", payment.path("currency").asText("INR"));
                            res.put("orderId", payment.path("order_id").asText(orderId));
                            return res;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch order payment status from Razorpay for order {}: {}", orderId, e.getMessage());
        }
        return null;
    }

    @Override
    public Map<String, Object> getPaymentDetails(String paymentId) {
        if (paymentId == null || paymentId.isBlank() || keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank() || keyId.startsWith("YOUR_")) {
            return null;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(keyId.trim(), keySecret.trim());
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = "https://api.razorpay.com/v1/payments/" + paymentId.trim();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode payment = objectMapper.readTree(response.getBody());
                Map<String, Object> res = new HashMap<>();
                res.put("paymentId", payment.path("id").asText(""));
                res.put("status", payment.path("status").asText(""));
                res.put("amount", payment.path("amount").asLong(0));
                res.put("currency", payment.path("currency").asText("INR"));
                res.put("orderId", payment.path("order_id").asText(""));
                return res;
            }
        } catch (Exception e) {
            log.warn("Could not fetch payment details from Razorpay for payment {}: {}", paymentId, e.getMessage());
        }
        return null;
    }

    private String calculateHmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM);
        mac.init(secretKey);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(rawHmac);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
