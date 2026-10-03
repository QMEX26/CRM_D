package com.crm.service;

import java.util.Map;

public interface RazorpayService {
    String createOrder(long amountInPaise, String currency, String receipt, Map<String, String> notes);
    boolean verifyPaymentSignature(String orderId, String paymentId, String signature);
    boolean verifyWebhookSignature(String requestBody, String signature);
    String getKeyId();
    Map<String, Object> fetchOrderPayment(String orderId);
    Map<String, Object> getPaymentDetails(String paymentId);
}

