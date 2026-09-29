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
public class CallSummaryStats {
    private long totalCalls;
    private long connectedCalls;
    private long noAnswerCalls;
    private long busyCalls;
    private long missedCalls;
    private long totalDurationSeconds;
    private LocalDateTime lastCallDate;
    private String lastCallBy;
    private String lastCallStatus;
    private String lastBusinessOutcome;
}
