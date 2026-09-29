package com.crm.service;

import com.crm.dto.request.FollowUpCreateRequest;
import com.crm.dto.request.FollowUpUpdateRequest;
import com.crm.dto.response.FollowUpResponse;
import com.crm.model.FollowUp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface FollowUpService {
    FollowUpResponse createFollowUp(FollowUpCreateRequest request, Long userId);
    FollowUpResponse createFollowUp(FollowUpCreateRequest request, Long userId, boolean isAdmin);
    FollowUpResponse updateFollowUp(Long id, FollowUpUpdateRequest request, Long userId, boolean isAdmin);
    FollowUpResponse toggleStatus(Long id, String status, Long userId, boolean isAdmin);
    List<FollowUp> getFollowUpsByLead(Long leadId);
    Page<FollowUpResponse> searchFollowUps(Long userId, String status, LocalDateTime start, LocalDateTime end, Pageable pageable);
    List<FollowUpResponse> getOverdueFollowUps(Long userId);
    List<FollowUpResponse> getTodayFollowUps(Long userId);
    List<FollowUpResponse> getUpcomingFollowUps(Long userId);
}
