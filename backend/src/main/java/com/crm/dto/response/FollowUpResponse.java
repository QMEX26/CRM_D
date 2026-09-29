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
public class FollowUpResponse {
    private Long id;
    private Long leadId;
    private String leadName;
    private String leadPhone;
    private Long projectId;
    private String projectName;
    private Long userId;
    private String userName;
    private LocalDateTime scheduledTime;
    private String status; // PENDING, COMPLETED, MISSED, CANCELLED
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private boolean isOverdue;
}
