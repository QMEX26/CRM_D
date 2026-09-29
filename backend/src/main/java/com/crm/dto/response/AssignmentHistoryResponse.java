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
public class AssignmentHistoryResponse {
    private Long id;
    private Long leadId;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long assignedById;
    private String assignedByName;
    private LocalDateTime assignedAt;
    private LocalDateTime unassignedAt;
    private Boolean isActive;
}
