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
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private long totalLeads;
    private long assignedLeads;
    private long assignedLeadsCount;
    private long convertedLeads;
}
