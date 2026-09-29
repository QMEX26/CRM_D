package com.crm.service;

import com.crm.dto.request.ShiftChangeCreateRequest;
import com.crm.dto.request.ShiftReviewRequest;
import com.crm.dto.response.ShiftChangeResponse;
import com.crm.dto.response.ShiftOptionResponse;

import java.util.List;

public interface ShiftService {
    List<ShiftOptionResponse> getAvailableShifts();

    ShiftChangeResponse createShiftChangeRequest(Long userId, ShiftChangeCreateRequest request);

    List<ShiftChangeResponse> getMyShiftRequests(Long userId);

    List<ShiftChangeResponse> getAllPendingShiftRequests();

    List<ShiftChangeResponse> getAllShiftRequests();

    ShiftChangeResponse reviewShiftChangeRequest(Long requestId, Long adminId, ShiftReviewRequest request);
}
