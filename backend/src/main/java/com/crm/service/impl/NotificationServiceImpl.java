package com.crm.service.impl;

import com.crm.dto.response.NotificationResponse;
import com.crm.model.Lead;
import com.crm.model.Notification;
import com.crm.model.User;
import com.crm.repository.NotificationRepository;
import com.crm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void createLeadAssignedNotification(Lead lead, User targetUser) {
        if (lead == null || targetUser == null) return;

        String projectName = lead.getProject() != null ? lead.getProject().getName() : "CRM Project";
        String title = "New Lead Assigned";
        String message = lead.getName() + " has been assigned to you in project " + projectName + ".";

        Notification notification = Notification.builder()
                .user(targetUser)
                .type("LEAD_ASSIGNMENT")
                .title(title)
                .message(message)
                .leadId(lead.getId())
                .leadName(lead.getName())
                .projectName(projectName)
                .referenceId(lead.getId())
                .referenceType("Lead")
                .status("NEW")
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void createPromotionDecisionNotification(User targetUser, boolean isApproved, String adminNotes, Long requestId) {
        if (targetUser == null) return;

        String title = isApproved ? "🟢 Promotion Request Approved" : "🔴 Promotion Request Rejected";
        String message = isApproved
                ? "Your request for Admin privileges has been approved. You now have Administrator access."
                : "Your request for Admin privileges has been rejected." + (adminNotes != null && !adminNotes.isBlank() ? " Note: " + adminNotes : "");
        String status = isApproved ? "APPROVED" : "REJECTED";

        Notification notification = Notification.builder()
                .user(targetUser)
                .type(isApproved ? "PROMOTION_APPROVED" : "PROMOTION_REJECTED")
                .title(title)
                .message(message)
                .referenceId(requestId)
                .referenceType("AdminAccessRequest")
                .status(status)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void markNotificationAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUser() != null && n.getUser().getId().equals(userId)) {
                n.setIsRead(true);
                n.setReadAt(LocalDateTime.now());
                notificationRepository.save(n);
            }
        });
    }

    @Override
    @Transactional
    public void markAllNotificationsAsRead(Long userId) {
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Notification n : list) {
            if (!Boolean.TRUE.equals(n.getIsRead())) {
                n.setIsRead(true);
                n.setReadAt(LocalDateTime.now());
            }
        }
        notificationRepository.saveAll(list);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId().toString())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .leadId(n.getLeadId())
                .leadName(n.getLeadName())
                .projectName(n.getProjectName())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .status(n.getStatus())
                .read(Boolean.TRUE.equals(n.getIsRead()))
                .createdAt(n.getCreatedAt())
                .build();
    }
}
