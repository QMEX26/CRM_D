package com.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallDashboardStatsResponse {

    // Call metrics
    private long totalCalls;
    private long connectedCalls;
    private long missedCalls;
    private long rejectedCalls;
    private long failedCalls;
    private long cancelledCalls;
    private long shortCalls;
    private long junkCalls;
    private long followUpCalls;
    private long totalDurationSeconds;
    private double averageDurationSeconds;

    // Follow-up metrics
    private long upcomingFollowUps;
    private long todayFollowUps;
    private long missedFollowUps;
    private long completedFollowUps;

    // Lead pipeline metrics
    private long totalLeads;
    private long contactedLeads;
    private long notContactedLeads;
    private long interestedLeads;
    private long notInterestedLeads;
    private long followUpLeads;
    private long convertedLeads;
    private long junkLeads;

    // Breakdowns
    private Map<String, Long> callsByLifecycleStatus;
    private Map<String, Long> callsByBusinessClassification;
    private Map<String, Long> leadsByStatus;
}
