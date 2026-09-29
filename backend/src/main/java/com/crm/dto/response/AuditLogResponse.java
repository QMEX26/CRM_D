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
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String entityName;
    private Long entityId;
    private String action;
    private String oldValue;
    private String newValue;
    private LocalDateTime createdAt;
}
