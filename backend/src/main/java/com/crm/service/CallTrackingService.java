package com.crm.service;

import com.crm.dto.request.CallClassificationUpdateRequest;
import com.crm.dto.request.CallEventRequest;
import com.crm.dto.response.CallDashboardStatsResponse;
import com.crm.dto.response.CallResponse;
import com.crm.dto.response.LeadTimelineItemResponse;
import com.crm.model.Lead;

import java.time.LocalDateTime;
import java.util.List;

public interface CallTrackingService {

    /**
     * Ingests and processes a call lifecycle event with strict idempotency,
     * status transitions, automatic classification, and lead metric updates.
     */
    CallResponse processCallEvent(CallEventRequest request, Long userId, boolean isAdmin);

    /**
     * Manually updates a call's business classification with full audit history.
     */
    CallResponse updateCallClassification(Long callId, CallClassificationUpdateRequest request, Long userId, boolean isAdmin);

    /**
     * Returns a chronological unified timeline for a lead (Calls, Follow-ups, Status changes).
     */
    List<LeadTimelineItemResponse> getLeadTimeline(Long leadId, Long userId, boolean isAdmin);

    /**
     * Derives real-time dashboard statistics dynamically from Call, Lead, and FollowUp tables.
     */
    CallDashboardStatsResponse getCallDashboardStats(Long userId, Long projectId, Long leadId,
                                                    LocalDateTime startDate, LocalDateTime endDate,
                                                    boolean isAdmin);

    /**
     * Recalculates summary counters for a lead based on full call history.
     */
    void recalculateLeadMetrics(Lead lead);
}
