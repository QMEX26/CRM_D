package com.crm.service;

import com.crm.dto.response.NotificationResponse;
import com.crm.model.Lead;
import com.crm.model.User;

import java.util.List;

public interface NotificationService {
    void createLeadAssignedNotification(Lead lead, User targetUser);
    void createPromotionDecisionNotification(User targetUser, boolean isApproved, String adminNotes, Long requestId);
    List<NotificationResponse> getUserNotifications(Long userId);
    void markNotificationAsRead(Long notificationId, Long userId);
    void markAllNotificationsAsRead(Long userId);
    long getUnreadCount(Long userId);
}
