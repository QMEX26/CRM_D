package com.crm.service;

import com.crm.dto.response.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {
    void logAction(Long userId, String entityName, Long entityId, String action, String oldValue, String newValue);
    Page<AuditLogResponse> searchAuditLogs(String entityName, Long userId, String action, Pageable pageable);
}
