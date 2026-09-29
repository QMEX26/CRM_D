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
public class AdminAccessRequestResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String status; // PENDING, APPROVED, REJECTED, NOT_REQUESTED
    private String reason;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
    private String adminNotes;
}
