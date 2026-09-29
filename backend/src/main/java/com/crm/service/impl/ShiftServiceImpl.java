package com.crm.service.impl;

import com.crm.dto.request.ShiftChangeCreateRequest;
import com.crm.dto.request.ShiftReviewRequest;
import com.crm.dto.response.ShiftChangeResponse;
import com.crm.dto.response.ShiftOptionResponse;
import com.crm.exception.BusinessException;
import com.crm.exception.ResourceNotFoundException;
import com.crm.model.Notification;
import com.crm.model.ShiftChangeRequest;
import com.crm.model.User;
import com.crm.model.WorkShift;
import com.crm.repository.NotificationRepository;
import com.crm.repository.ShiftChangeRequestRepository;
import com.crm.repository.UserRepository;
import com.crm.service.AuditService;
import com.crm.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Kolkata");

    private final ShiftChangeRequestRepository shiftChangeRequestRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final AuditService auditService;

    @Override
    public List<ShiftOptionResponse> getAvailableShifts() {
        List<ShiftOptionResponse> list = new ArrayList<>();
        for (WorkShift ws : WorkShift.getAllShifts()) {
            list.add(ShiftOptionResponse.builder()
                    .id(ws.getId())
                    .displayName(ws.getDisplayName())
                    .startTime(ws.getStartTime())
                    .endTime(ws.getEndTime())
                    .isDefault(ws == WorkShift.SHIFT_1000_1900)
                    .build());
        }
        return list;
    }

    @Override
    @Transactional
    public ShiftChangeResponse createShiftChangeRequest(Long userId, ShiftChangeCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        WorkShift requested = WorkShift.fromString(request.getRequestedShift());
        WorkShift current = user.getShift() != null ? user.getShift() : WorkShift.SHIFT_1000_1900;

        if (requested == current) {
            throw new BusinessException("Requested shift (" + requested.getDisplayName() + ") is already your current assigned shift.");
        }

        if (shiftChangeRequestRepository.existsByUserIdAndStatus(userId, "PENDING")) {
            throw new BusinessException("You already have a shift change request pending approval.");
        }

        ShiftChangeRequest scr = ShiftChangeRequest.builder()
                .user(user)
                .currentShift(current)
                .requestedShift(requested)
                .status("PENDING")
                .reason(request.getReason())
                .build();

        ShiftChangeRequest saved = shiftChangeRequestRepository.save(scr);

        auditService.logAction(userId, "ShiftChangeRequest", saved.getId(), "CREATE",
                "Current: " + current.getDisplayName(), "Requested: " + requested.getDisplayName());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftChangeResponse> getMyShiftRequests(Long userId) {
        return shiftChangeRequestRepository.findByUserIdOrderByRequestedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftChangeResponse> getAllPendingShiftRequests() {
        return shiftChangeRequestRepository.findByStatusOrderByRequestedAtDesc("PENDING").stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftChangeResponse> getAllShiftRequests() {
        return shiftChangeRequestRepository.findAllByOrderByRequestedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ShiftChangeResponse reviewShiftChangeRequest(Long requestId, Long adminId, ShiftReviewRequest request) {
        ShiftChangeRequest scr = shiftChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift change request not found with id: " + requestId));

        if (!"PENDING".equalsIgnoreCase(scr.getStatus())) {
            throw new BusinessException("This shift change request has already been processed with status: " + scr.getStatus());
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));

        String newStatus = request.getStatus() != null ? request.getStatus().trim().toUpperCase() : "";
        if (!"APPROVED".equals(newStatus) && !"REJECTED".equals(newStatus)) {
            throw new BusinessException("Invalid review status. Must be APPROVED or REJECTED.");
        }

        User requester = scr.getUser();
        scr.setStatus(newStatus);
        scr.setAdminNotes(request.getAdminNotes());
        scr.setReviewedBy(admin);
        scr.setReviewedAt(LocalDateTime.now(APP_ZONE));

        if ("APPROVED".equals(newStatus)) {
            requester.setShift(scr.getRequestedShift());
            userRepository.save(requester);

            // User notification
            Notification notif = Notification.builder()
                    .user(requester)
                    .type("SHIFT_CHANGE_APPROVED")
                    .title("🟢 Shift Change Request Approved")
                    .message("Your shift change request has been approved.\n\nNew Shift: " + scr.getRequestedShift().getDisplayName())
                    .status("APPROVED")
                    .referenceId(scr.getId())
                    .referenceType("ShiftChangeRequest")
                    .isRead(false)
                    .build();
            notificationRepository.save(notif);
        } else {
            // User notification
            String rejectMsg = "Your shift change request to " + scr.getRequestedShift().getDisplayName() + " has been rejected.";
            if (request.getAdminNotes() != null && !request.getAdminNotes().isBlank()) {
                rejectMsg += "\nReason: " + request.getAdminNotes();
            }
            Notification notif = Notification.builder()
                    .user(requester)
                    .type("SHIFT_CHANGE_REJECTED")
                    .title("🔴 Shift Change Request Rejected")
                    .message(rejectMsg)
                    .status("REJECTED")
                    .referenceId(scr.getId())
                    .referenceType("ShiftChangeRequest")
                    .isRead(false)
                    .build();
            notificationRepository.save(notif);
        }

        ShiftChangeRequest saved = shiftChangeRequestRepository.save(scr);

        auditService.logAction(adminId, "ShiftChangeRequest", saved.getId(), "REVIEW",
                "PENDING", newStatus + " for user " + requester.getName());

        return toResponse(saved);
    }

    private ShiftChangeResponse toResponse(ShiftChangeRequest scr) {
        if (scr == null) return null;

        User u = scr.getUser();
        User reviewer = scr.getReviewedBy();
        WorkShift cur = scr.getCurrentShift() != null ? scr.getCurrentShift() : WorkShift.SHIFT_1000_1900;
        WorkShift req = scr.getRequestedShift() != null ? scr.getRequestedShift() : WorkShift.SHIFT_1000_1900;

        return ShiftChangeResponse.builder()
                .id(scr.getId())
                .userId(u != null ? u.getId() : null)
                .userName(u != null ? u.getName() : null)
                .userEmail(u != null ? u.getEmail() : null)
                .currentShift(cur.getId())
                .currentShiftDisplayName(cur.getDisplayName())
                .requestedShift(req.getId())
                .requestedShiftDisplayName(req.getDisplayName())
                .status(scr.getStatus())
                .reason(scr.getReason())
                .adminNotes(scr.getAdminNotes())
                .requestedAt(scr.getRequestedAt())
                .reviewedAt(scr.getReviewedAt())
                .reviewedById(reviewer != null ? reviewer.getId() : null)
                .reviewedByName(reviewer != null ? reviewer.getName() : null)
                .build();
    }
}
