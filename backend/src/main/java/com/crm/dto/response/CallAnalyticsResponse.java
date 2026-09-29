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
public class CallAnalyticsResponse {
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Analytics Cards
    private long totalCalls;
    private long uniqueCalls;
    private long notAttendedCalls;
    private long freshCalls;
    private long prospectCalls;
    private long junkCalls;
    private long acceptableCalls;

    // System Breakdown
    private long inboundCalls;
    private long outboundCalls;
    private long missedCalls;
    private long shortCalls;
    private long totalTalkTimeSeconds;
    private long averageDurationSeconds;
    private long connectedCalls;
    private long todayCalls;
    private long upcomingFollowUps;
    private long missedFollowUps;
}
