package com.crm.controller;

import com.crm.dto.request.ShiftChangeCreateRequest;
import com.crm.dto.request.ShiftReviewRequest;
import com.crm.dto.response.ApiResponse;
import com.crm.dto.response.ShiftChangeResponse;
import com.crm.dto.response.ShiftOptionResponse;
import com.crm.security.CurrentUser;
import com.crm.security.UserPrincipal;
import com.crm.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShiftOptionResponse>>> getAvailableShifts() {
        return ResponseEntity.ok(ApiResponse.ok(shiftService.getAvailableShifts()));
    }

    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<ShiftChangeResponse>> createShiftChangeRequest(
            @Valid @RequestBody ShiftChangeCreateRequest request,
            @CurrentUser UserPrincipal principal) {
        ShiftChangeResponse response = shiftService.createShiftChangeRequest(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Shift change request submitted successfully", response));
    }

    @GetMapping("/requests/my")
    public ResponseEntity<ApiResponse<List<ShiftChangeResponse>>> getMyShiftRequests(
            @CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(shiftService.getMyShiftRequests(principal.getId())));
    }

    @GetMapping("/requests/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ShiftChangeResponse>>> getPendingShiftRequests() {
        return ResponseEntity.ok(ApiResponse.ok(shiftService.getAllPendingShiftRequests()));
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ShiftChangeResponse>>> getAllShiftRequests() {
        return ResponseEntity.ok(ApiResponse.ok(shiftService.getAllShiftRequests()));
    }

    @PostMapping("/requests/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShiftChangeResponse>> reviewShiftChangeRequest(
            @PathVariable Long id,
            @Valid @RequestBody ShiftReviewRequest request,
            @CurrentUser UserPrincipal principal) {
        ShiftChangeResponse response = shiftService.reviewShiftChangeRequest(id, principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Shift change request processed", response));
    }
}
