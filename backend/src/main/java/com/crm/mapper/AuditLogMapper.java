package com.crm.mapper;

import com.crm.dto.response.AuditLogResponse;
import com.crm.model.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLog log) {
        if (log == null) return null;

        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .userName(log.getUser() != null ? log.getUser().getName() : "SYSTEM")
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
