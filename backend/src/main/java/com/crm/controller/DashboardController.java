package com.crm.controller;

import com.crm.dto.response.ApiResponse;
import com.crm.dto.response.DashboardSummaryResponse;
import com.crm.security.CurrentUser;
import com.crm.security.UserPrincipal;
import com.crm.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getAdminDashboard() {
        DashboardSummaryResponse response = dashboardService.getAdminDashboardSummary();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getUserDashboard(@CurrentUser UserPrincipal principal) {
        DashboardSummaryResponse response = dashboardService.getUserDashboardSummary(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
