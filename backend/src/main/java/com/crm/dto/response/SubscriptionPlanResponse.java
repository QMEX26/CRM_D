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
public class SubscriptionPlanResponse {
    private Long id;
    private String name;
    private String targetRole;
    private BigDecimal price;
    private String currency;
    private String billingCycle;
    private Integer trialDays;
    private Boolean active;
}
