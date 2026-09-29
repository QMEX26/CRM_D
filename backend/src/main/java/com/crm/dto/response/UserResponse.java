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
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String shift;
    private String shiftDisplayName;
    private java.time.LocalTime shiftStartTime;
    private java.time.LocalTime shiftEndTime;
    private LocalDateTime createdAt;
    private long activeLeadsCount;
    private long totalCallsCount;
}
