package com.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceMonthlyResponse {
    private int year;
    private int month;
    private long totalDays;
    private long presentDays;
    private long fullDays;
    private long halfDays;
    private long offDays;
    private long leaveDays;
    private long holidayDays;
    private long totalWorkingHours;
    private List<AttendanceResponse> records;
}
