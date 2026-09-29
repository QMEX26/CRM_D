package com.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsDashboardResponse {

    private KpiMetrics kpis;
    private CallPerformanceMetrics callPerformance;
    private LeadPerformanceMetrics leadPerformance;
    private List<ProjectStat> projectPerformance;
    private List<UserStat> userActivity;
    private FollowUpMetrics followUpReport;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiMetrics {
        private long totalLeads;
        private long totalCalls;
        private long connectedCalls;
        private long missedCalls;
        private long totalTalkTimeSeconds;
        private long avgTalkTimeSeconds;
        private long conversions;
        private double conversionRate;
        private long pendingFollowUps;
        private long overdueFollowUps;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CallPerformanceMetrics {
        private long connectedCount;
        private long missedCount;
        private long inboundCount;
        private long outboundCount;
        private Map<String, Long> classificationBreakdown;
        private List<TimePoint> timelineData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeadPerformanceMetrics {
        private Map<String, Long> stageBreakdown;
        private Map<String, Long> outcomeBreakdown;
        private double conversionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectStat {
        private Long projectId;
        private String projectName;
        private long totalLeads;
        private long totalCalls;
        private long conversions;
        private double conversionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStat {
        private Long userId;
        private String userName;
        private String role;
        private long assignedLeads;
        private long totalCalls;
        private long connectedCalls;
        private long talkTimeSeconds;
        private long conversions;
        private long followUpsHandled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FollowUpMetrics {
        private long pendingCount;
        private long completedCount;
        private long overdueCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimePoint {
        private String label;
        private long calls;
        private long connected;
    }
}
