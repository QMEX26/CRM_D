package com.crm.service;

import com.crm.dto.request.CallCreateRequest;
import com.crm.dto.response.CallResponse;
import com.crm.dto.response.CallSummaryStats;
import com.crm.model.Call;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface CallService {
    CallResponse logCall(CallCreateRequest request, Long userId, boolean isAdmin);
    CallSummaryStats getCallSummaryStats(Long leadId);
    CallSummaryStats getCallSummaryStats(Long leadId, Long userId, boolean isAdmin);
    List<Call> getCallsByLead(Long leadId);
    List<Call> getCallsByLead(Long leadId, Long userId, boolean isAdmin);
    Page<CallResponse> searchCalls(Long userId, Long leadId, Long projectId, String status, String outcome,
                                   LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    com.crm.dto.response.CallAnalyticsResponse getCallAnalytics(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}
