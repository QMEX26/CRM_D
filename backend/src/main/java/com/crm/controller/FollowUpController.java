package com.crm.controller;

import com.crm.dto.request.FollowUpCreateRequest;
import com.crm.dto.request.FollowUpStatusRequest;
import com.crm.dto.request.FollowUpUpdateRequest;
import com.crm.dto.response.ApiResponse;
import com.crm.dto.response.FollowUpResponse;
import com.crm.dto.response.PageResponse;
import com.crm.security.CurrentUser;
import com.crm.security.UserPrincipal;
import com.crm.service.FollowUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/follow-ups")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FollowUpResponse>>> searchFollowUps(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @CurrentUser UserPrincipal principal) {

        Long effectiveUserId = principal.isAdmin() ? userId : principal.getId();
        Page<FollowUpResponse> result = followUpService.searchFollowUps(effectiveUserId, status, start, end, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result)));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<FollowUpResponse>>> getTodayFollowUps(@CurrentUser UserPrincipal principal) {
        Long effectiveUserId = principal.isAdmin() ? null : principal.getId();
        return ResponseEntity.ok(ApiResponse.ok(followUpService.getTodayFollowUps(effectiveUserId)));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<FollowUpResponse>>> getUpcomingFollowUps(@CurrentUser UserPrincipal principal) {
        Long effectiveUserId = principal.isAdmin() ? null : principal.getId();
        return ResponseEntity.ok(ApiResponse.ok(followUpService.getUpcomingFollowUps(effectiveUserId)));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<FollowUpResponse>>> getOverdueFollowUps(@CurrentUser UserPrincipal principal) {
        Long effectiveUserId = principal.isAdmin() ? null : principal.getId();
        return ResponseEntity.ok(ApiResponse.ok(followUpService.getOverdueFollowUps(effectiveUserId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FollowUpResponse>> createFollowUp(
            @Valid @RequestBody FollowUpCreateRequest request,
            @CurrentUser UserPrincipal principal) {

        FollowUpResponse response = followUpService.createFollowUp(request, principal.getId(), principal.isAdmin());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Follow-up scheduled successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FollowUpResponse>> updateFollowUp(
            @PathVariable Long id,
            @Valid @RequestBody FollowUpUpdateRequest request,
            @CurrentUser UserPrincipal principal) {

        FollowUpResponse response = followUpService.updateFollowUp(id, request, principal.getId(), principal.isAdmin());
        return ResponseEntity.ok(ApiResponse.ok("Follow-up updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<FollowUpResponse>> toggleStatus(
            @PathVariable Long id,
            @Valid @RequestBody FollowUpStatusRequest request,
            @CurrentUser UserPrincipal principal) {

        FollowUpResponse response = followUpService.toggleStatus(id, request.getStatus(), principal.getId(), principal.isAdmin());
        return ResponseEntity.ok(ApiResponse.ok("Follow-up status updated successfully", response));
    }
}
