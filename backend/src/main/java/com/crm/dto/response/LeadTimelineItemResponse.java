package com.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadTimelineItemResponse {

    /**
     * Type of timeline event: CALL, FOLLOW_UP, NOTE, STATUS_CHANGE, LEAD_CREATED
     */
    private String type;

    private Long id;
    private Long leadId;
    private Long userId;
    private String userName;

    private LocalDateTime timestamp;

    // Call-specific fields
    private Integer durationSeconds;
    private String callDirection;
    private String technicalStatus;
    private String businessClassification;
    private Boolean classificationChangedManually;

    // Follow-up specific fields
    private LocalDateTime scheduledTime;
    private String followUpStatus;

    // General
    private String title;
    private String notes;
    private Boolean followUpRequired;
    private LocalDateTime nextFollowUpAt;
}
