package com.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleResponse {
    private Long id;
    private Long leadId;
    private String leadName;
    private String leadPhone;
    private Long projectId;
    private String projectName;
    private Long userId;
    private String userName;
    private String assignedAgentName;
    private String status;
    private BigDecimal dealValue;
    private String notes;
    private LocalDateTime convertedAt;
    private LocalDateTime createdAt;
}
