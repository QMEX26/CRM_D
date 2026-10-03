package com.crm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionOrderRequest {
    private Long planId; // Optional; backend resolves by authenticated user's role
}
