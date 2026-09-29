package com.crm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowUpUpdateRequest {
    private LocalDateTime scheduledTime;
    private String status; // PENDING, COMPLETED, MISSED, CANCELLED
    private String notes;
}
