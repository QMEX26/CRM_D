package com.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftChangeResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String currentShift;
    private String currentShiftDisplayName;
    private String requestedShift;
    private String requestedShiftDisplayName;
    private String status; // PENDING, APPROVED, REJECTED
    private String reason;
    private String adminNotes;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedById;
    private String reviewedByName;
}
