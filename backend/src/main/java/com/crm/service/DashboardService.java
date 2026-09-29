package com.crm.service;

import com.crm.dto.response.DashboardSummaryResponse;

public interface DashboardService {
    DashboardSummaryResponse getAdminDashboardSummary();
    DashboardSummaryResponse getUserDashboardSummary(Long userId);
}
