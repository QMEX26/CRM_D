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
public class CallResponse {
    private Long id;
    private String telephonyCallId;
    private Long leadId;
    private String leadName;
    private String leadPhone;
    private String phoneNumber;
    private Boolean isConnected;
    private Long userId;
    private String userName;
    private String callDirection;
    private String callLifecycleStatus;
    private LocalDateTime startedAt;
    private LocalDateTime connectedAt;
    private LocalDateTime endedAt;
    private Integer durationSeconds;
    private String callStatus;
    private String businessOutcome;
    private String automaticClassification;
    private String finalClassification;
    private Boolean classificationChangedManually;
    private String classificationChangedByName;
    private LocalDateTime classificationChangedAt;
    private Boolean followUpRequired;
    private LocalDateTime followUpDate;
    private Long followUpId;
    private String notes;
    private LocalDateTime createdAt;
}
