package com.crm.mapper;

import com.crm.dto.response.FollowUpResponse;
import com.crm.model.FollowUp;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FollowUpMapper {

    public FollowUpResponse toResponse(FollowUp followUp) {
        if (followUp == null) return null;

        boolean isOverdue = "PENDING".equalsIgnoreCase(followUp.getStatus()) &&
                followUp.getScheduledTime().isBefore(LocalDateTime.now());

        return FollowUpResponse.builder()
                .id(followUp.getId())
                .leadId(followUp.getLead() != null ? followUp.getLead().getId() : null)
                .leadName(followUp.getLead() != null ? followUp.getLead().getName() : null)
                .leadPhone(followUp.getLead() != null ? followUp.getLead().getPhone() : null)
                .projectId(followUp.getLead() != null && followUp.getLead().getProject() != null ? followUp.getLead().getProject().getId() : null)
                .projectName(followUp.getLead() != null && followUp.getLead().getProject() != null ? followUp.getLead().getProject().getName() : null)
                .userId(followUp.getUser() != null ? followUp.getUser().getId() : null)
                .userName(followUp.getUser() != null ? followUp.getUser().getName() : null)
                .scheduledTime(followUp.getScheduledTime())
                .status(followUp.getStatus())
                .notes(followUp.getNotes())
                .createdAt(followUp.getCreatedAt())
                .completedAt(followUp.getCompletedAt())
                .isOverdue(isOverdue)
                .build();
    }
}
