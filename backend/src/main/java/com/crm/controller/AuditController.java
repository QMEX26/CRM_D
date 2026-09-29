package com.crm.controller;

import com.crm.dto.response.ApiResponse;
import com.crm.dto.response.AuditLogResponse;
import com.crm.dto.response.PageResponse;
import com.crm.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> searchAuditLogs(
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        Page<AuditLogResponse> result = auditService.searchAuditLogs(entityName, userId, action,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result)));
    }
}
