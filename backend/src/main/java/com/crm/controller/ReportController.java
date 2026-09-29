package com.crm.controller;

import com.crm.dto.response.ApiResponse;
import com.crm.dto.response.CallResponse;
import com.crm.dto.response.LeadSummaryResponse;
import com.crm.dto.response.PageResponse;
import com.crm.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/leads")
    public ResponseEntity<ApiResponse<PageResponse<LeadSummaryResponse>>> getLeadReport(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String outcome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        Page<LeadSummaryResponse> result = reportService.getLeadReport(projectId, status, outcome, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result)));
    }

    @GetMapping("/calls")
    public ResponseEntity<ApiResponse<PageResponse<CallResponse>>> getCallReport(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        Page<CallResponse> result = reportService.getCallReport(userId, projectId, status, start, end, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result)));
    }

    @GetMapping("/employees")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEmployeeActivityReport() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getEmployeeActivityReport()));
    }

    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getProjectReport() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getProjectReport()));
    }

    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        return ResponseEntity.ok(ApiResponse.ok(reportService.getSalesReport(start, end)));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<com.crm.dto.response.AdminAnalyticsDashboardResponse>> getAdminAnalyticsDashboard(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String leadStatus,
            @RequestParam(required = false) String callStatus,
            @RequestParam(required = false) String callDirection,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        com.crm.dto.response.AdminAnalyticsDashboardResponse dashboard = reportService.getAdminAnalyticsDashboard(projectId, userId, leadStatus, callStatus, callDirection, start, end);
        return ResponseEntity.ok(ApiResponse.ok(dashboard));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReportCsv(
            @RequestParam(defaultValue = "LEADS") String type,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String leadStatus,
            @RequestParam(required = false) String callStatus,
            @RequestParam(required = false) String callDirection,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        byte[] csvData = reportService.exportReportCsv(type, projectId, userId, leadStatus, callStatus, callDirection, start, end);
        String filename = "report_" + type.toLowerCase() + "_" + System.currentTimeMillis() + ".csv";

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(csvData);
    }
}
