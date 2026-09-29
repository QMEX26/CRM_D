package com.crm.service;

import com.crm.dto.response.AttendanceMonthlyResponse;
import com.crm.dto.response.AttendanceResponse;

public interface AttendanceService {
    AttendanceResponse getTodayAttendance(Long userId);
    AttendanceResponse clockIn(Long userId);
    AttendanceResponse clockOut(Long userId);
    AttendanceMonthlyResponse getMonthlyAttendance(Long userId, int year, int month);
}
