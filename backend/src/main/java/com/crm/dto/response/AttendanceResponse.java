package com.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {
    private Long id;
    private Long userId;
    private String userName;
    private LocalDate date;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private Integer durationMinutes;
    private String status; // PRESENT, HALF_DAY, LEAVE, HOLIDAY
    private String notes;
    private boolean clockedIn;
    private boolean clockedOut;
    private String shift;
    private String shiftDisplayName;
    private java.time.LocalTime shiftStartTime;
    private java.time.LocalTime shiftEndTime;
    private boolean checkInOverdue;
    private LocalDateTime serverTime;
}
