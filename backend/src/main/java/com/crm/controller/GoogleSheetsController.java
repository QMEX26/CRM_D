package com.crm.controller;

import com.crm.dto.request.GoogleSheetsPushRequest;
import com.crm.dto.response.ApiResponse;
import com.crm.dto.response.GoogleSheetsPullResponse;
import com.crm.dto.response.GoogleSheetsPushResponse;
import com.crm.dto.response.GoogleSheetsSyncResponse;
import com.crm.dto.response.PageResponse;
import com.crm.model.GoogleSheetsSyncLog;
import com.crm.security.CurrentUser;
import com.crm.security.UserPrincipal;
import com.crm.service.GoogleSheetsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/google-sheets", "/api/v1/admin/google-sheets", "/api/v1/google-sheet/sync"})
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GoogleSheetsController {

    private final GoogleSheetsService googleSheetsService;

    @PostMapping({"/pull", "/sync/pull"})
    public ResponseEntity<ApiResponse<GoogleSheetsPullResponse>> pullDatabaseSnapshot(@CurrentUser UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : 1L;
        GoogleSheetsPullResponse response = googleSheetsService.pullDatabaseSnapshot(userId);
        return ResponseEntity.ok(ApiResponse.ok("Database snapshot pulled successfully into Google Sheets format", response));
    }

    @PostMapping({"/push", "/sync/push"})
    public ResponseEntity<ApiResponse<GoogleSheetsPushResponse>> pushDatabaseSnapshot(
            @RequestBody GoogleSheetsPushRequest request,
            @CurrentUser UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : 1L;
        GoogleSheetsPushResponse response = googleSheetsService.pushDatabaseSnapshot(request, userId);
        return ResponseEntity.ok(ApiResponse.ok("Database synchronized successfully from Google Sheets", response));
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<GoogleSheetsSyncResponse>> triggerSync(@CurrentUser UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : 1L;
        GoogleSheetsSyncResponse response = googleSheetsService.triggerSync(userId);
        if ("FAILED".equals(response.getStatus())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, response.getMessage(), response));
        }
        return ResponseEntity.ok(ApiResponse.ok("CRM data synchronized successfully into Google Sheets", response));
    }

    @GetMapping({"/status", "/sync/status"})
    public ResponseEntity<ApiResponse<GoogleSheetsSyncResponse>> getSyncStatus() {
        return ResponseEntity.ok(ApiResponse.ok(googleSheetsService.getLatestSyncStatus()));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponse<GoogleSheetsSyncLog>>> getSyncHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<GoogleSheetsSyncLog> result = googleSheetsService.getSyncHistory(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result)));
    }
}
