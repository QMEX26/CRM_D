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
public class NotificationResponse {
    private String id;
    private String type; // PERMISSION_REQUEST, LEAD_ASSIGNMENT, LEAD_REASSIGNMENT, PROJECT_EVENT, SYSTEM_EVENT, PROMOTION_APPROVED, PROMOTION_REJECTED
    private String title;
    private String message;
    private LocalDateTime createdAt;
    private boolean read;
    private Long referenceId;
    private String referenceType;
    private String status; // PENDING, APPROVED, REJECTED, INFO, NEW
    private Long leadId;
    private String leadName;
    private String projectName;
}
