package com.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayOrderResponse {
    private String orderId;
    private Long amountInPaise;
    private BigDecimal amount;
    private String currency;
    private String keyId;
    private String planName;
    private String userEmail;
    private String userName;
    private String userPhone;
}
