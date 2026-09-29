package com.crm.service;

import com.crm.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReportService {
    Page<LeadSummaryResponse> getLeadReport(Long projectId, String status, String outcome, Pageable pageable);
    Page<CallResponse> getCallReport(Long userId, Long projectId, String status, LocalDateTime start, LocalDateTime end, Pageable pageable);
    List<Map<String, Object>> getEmployeeActivityReport();
    List<Map<String, Object>> getProjectReport();
    List<Map<String, Object>> getSalesReport(LocalDateTime start, LocalDateTime end);

    AdminAnalyticsDashboardResponse getAdminAnalyticsDashboard(Long projectId, Long userId, String leadStatus, String callStatus, String callDirection, LocalDateTime start, LocalDateTime end);
    byte[] exportReportCsv(String reportType, Long projectId, Long userId, String leadStatus, String callStatus, String callDirection, LocalDateTime start, LocalDateTime end);
}
