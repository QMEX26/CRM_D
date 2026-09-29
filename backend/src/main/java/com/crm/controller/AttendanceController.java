package com.crm.controller;

import com.crm.dto.response.ApiResponse;
import com.crm.dto.response.AttendanceMonthlyResponse;
import com.crm.dto.response.AttendanceResponse;
import com.crm.security.CurrentUser;
import com.crm.security.UserPrincipal;
import com.crm.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.crm.exception.ForbiddenException;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Kolkata");
    private final AttendanceService attendanceService;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getTodayAttendance(@CurrentUser UserPrincipal principal) {
        AttendanceResponse response = attendanceService.getTodayAttendance(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/clock-in")
    public ResponseEntity<ApiResponse<AttendanceResponse>> clockIn(@CurrentUser UserPrincipal principal) {
        AttendanceResponse response = attendanceService.clockIn(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Clocked in successfully", response));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<ApiResponse<AttendanceResponse>> clockOut(@CurrentUser UserPrincipal principal) {
        AttendanceResponse response = attendanceService.clockOut(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Clocked out successfully", response));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<AttendanceMonthlyResponse>> getMonthlyAttendance(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long userId,
            @CurrentUser UserPrincipal principal) {

        Long targetUserId = (userId != null && (principal.isAdmin() || principal.getId().equals(userId)))
                ? userId
                : principal.getId();

        LocalDate now = LocalDate.now(APP_ZONE);
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        AttendanceMonthlyResponse response = attendanceService.getMonthlyAttendance(targetUserId, targetYear, targetMonth);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/users/{userId}/monthly")
    public ResponseEntity<ApiResponse<AttendanceMonthlyResponse>> getUserMonthlyAttendance(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @CurrentUser UserPrincipal principal) {

        if (!principal.isAdmin() && !principal.getId().equals(userId)) {
            throw new ForbiddenException("Only administrators can view attendance for other users.");
        }

        LocalDate now = LocalDate.now(APP_ZONE);
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        AttendanceMonthlyResponse response = attendanceService.getMonthlyAttendance(userId, targetYear, targetMonth);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
