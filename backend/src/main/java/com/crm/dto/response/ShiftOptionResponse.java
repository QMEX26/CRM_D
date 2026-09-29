package com.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftOptionResponse {
    private String id;
    private String displayName;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isDefault;
}
