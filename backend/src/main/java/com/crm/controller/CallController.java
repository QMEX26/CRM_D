package com.crm.controller;

import com.crm.dto.request.CallCreateRequest;
import com.crm.dto.response.ApiResponse;
import com.crm.dto.response.CallResponse;
import com.crm.dto.response.CallSummaryStats;
import com.crm.dto.response.PageResponse;
import com.crm.mapper.CallMapper;
import com.crm.model.Call;
import com.crm.security.CurrentUser;
import com.crm.security.UserPrincipal;
import com.crm.service.CallService;
import com.crm.service.CallTrackingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CallController {

    private final CallService callService;
    private final CallTrackingService callTrackingService;
    private final CallMapper callMapper;

    public CallController(CallService callService, CallTrackingService callTrackingService, CallMapper callMapper) {
        this.callService = callService;
        this.callTrackingService = callTrackingService;
        this.callMapper = callMapper;
    }

    @PostMapping("/calls/events")
    public ResponseEntity<ApiResponse<CallResponse>> processCallEvent(
            @Valid @RequestBody com.crm.dto.request.CallEventRequest request,
            @CurrentUser UserPrincipal principal) {
        CallResponse response = callTrackingService.processCallEvent(request, principal.getId(), principal.isAdmin());
        return ResponseEntity.ok(ApiResponse.ok("Call event processed successfully", response));
    }

    @PatchMapping("/calls/{id}/classification")
    public ResponseEntity<ApiResponse<CallResponse>> updateCallClassification(
            @PathVariable Long id,
            @Valid @RequestBody com.crm.dto.request.CallClassificationUpdateRequest request,
            @CurrentUser UserPrincipal principal) {
        CallResponse response = callTrackingService.updateCallClassification(id, request, principal.getId(), principal.isAdmin());
        return ResponseEntity.ok(ApiResponse.ok("Call classification updated successfully", response));
    }

    @GetMapping("/leads/{id}/timeline")
    public ResponseEntity<ApiResponse<List<com.crm.dto.response.LeadTimelineItemResponse>>> getLeadTimeline(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        List<com.crm.dto.response.LeadTimelineItemResponse> timeline = callTrackingService.getLeadTimeline(id, principal.getId(), principal.isAdmin());
        return ResponseEntity.ok(ApiResponse.ok(timeline));
    }

    @GetMapping("/calls/dashboard-stats")
    public ResponseEntity<ApiResponse<com.crm.dto.response.CallDashboardStatsResponse>> getCallDashboardStats(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long leadId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @CurrentUser UserPrincipal principal) {
        Long effectiveUserId = principal.isAdmin() ? userId : principal.getId();
        com.crm.dto.response.CallDashboardStatsResponse stats = callTrackingService.getCallDashboardStats(
                effectiveUserId, projectId, leadId, startDate, endDate, principal.isAdmin());
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/calls")
    public ResponseEntity<ApiResponse<PageResponse<CallResponse>>> searchCalls(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long leadId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @CurrentUser UserPrincipal principal) {

        Long effectiveUserId = principal.isAdmin() ? userId : principal.getId();
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Page<CallResponse> result = callService.searchCalls(effectiveUserId, leadId, projectId, status, outcome,
                startDate, endDate, PageRequest.of(page, size, sort));

        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result)));
    }

    @PostMapping("/calls")
    public ResponseEntity<ApiResponse<CallResponse>> logCall(
            @Valid @RequestBody CallCreateRequest request,
            @CurrentUser UserPrincipal principal) {

        CallResponse response = callService.logCall(request, principal.getId(), principal.isAdmin());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Call logged successfully", response));
    }

    @GetMapping("/leads/{id}/calls")
    public ResponseEntity<ApiResponse<List<CallResponse>>> getCallsForLead(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        List<Call> calls = callService.getCallsByLead(id, principal.getId(), principal.isAdmin());
        List<CallResponse> dtos = calls.stream().map(callMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    @GetMapping("/leads/{id}/call-stats")
    public ResponseEntity<ApiResponse<CallSummaryStats>> getCallStatsForLead(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        CallSummaryStats stats = callService.getCallSummaryStats(id, principal.getId(), principal.isAdmin());
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/calls/analytics")
    public ResponseEntity<ApiResponse<com.crm.dto.response.CallAnalyticsResponse>> getCallAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @CurrentUser UserPrincipal principal) {
        com.crm.dto.response.CallAnalyticsResponse stats = callService.getCallAnalytics(principal.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
