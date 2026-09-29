package com.crm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleSheetsSyncPayload {

    private String secret;
    private String syncId;
    private String timestamp;
    private String triggeredBy;
    @Builder.Default
    private String action = "sync";

    @Builder.Default
    private List<UserSyncItem> users = new ArrayList<>();

    @Builder.Default
    private List<ProjectSyncItem> projects = new ArrayList<>();

    @Builder.Default
    private List<LeadSyncItem> leads = new ArrayList<>();

    @Builder.Default
    private List<AssignmentSyncItem> assignments = new ArrayList<>();

    @Builder.Default
    private List<CallSyncItem> calls = new ArrayList<>();

    @Builder.Default
    private List<FollowUpSyncItem> followUps = new ArrayList<>();

    @Builder.Default
    private List<SaleSyncItem> sales = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserSyncItem {
        private Long id;
        private String employeeId;
        private String name;
        private String email;
        private String phone;
        private String role;
        private String status;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjectSyncItem {
        private Long id;
        private String name;
        private String description;
        private String status;
        private String createdBy;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LeadSyncItem {
        private Long id;
        private Long projectId;
        private String projectName;
        private String name;
        private String phone;
        private String alternatePhone;
        private String email;
        private String company;
        private String location;
        private String city;
        private String state;
        private String source;
        private String status;
        private String priority;
        private String businessOutcome;
        private Boolean interested;
        private Boolean convertedToSale;
        private String notes;
        private Integer totalCallCount;
        private Integer connectedCallCount;
        private Integer missedCallCount;
        private String lastCallStatus;
        private String nextFollowUpAt;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignmentSyncItem {
        private Long id;
        private Long leadId;
        private String leadName;
        private Long projectId;
        private String projectName;
        private Long assignedUserId;
        private String assignedUserName;
        private String assignedBy;
        private Boolean isActive;
        private String assignedAt;
        private String unassignedAt;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CallSyncItem {
        private Long id;
        private Long leadId;
        private String leadName;
        private Long projectId;
        private String projectName;
        private Long callerUserId;
        private String callerName;
        private String phoneNumber;
        private String callDirection;
        private String callStatus;
        private String callLifecycleStatus;
        private String startedAt;
        private String endedAt;
        private Integer durationSeconds;
        private String businessOutcome;
        private String automaticClassification;
        private String finalClassification;
        private String notes;
        private Boolean followUpRequired;
        private String followUpDate;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FollowUpSyncItem {
        private Long id;
        private Long leadId;
        private String leadName;
        private Long projectId;
        private String assignedUser;
        private Long callId;
        private String followUpDate;
        private String followUpTime;
        private String status;
        private String notes;
        private String createdAt;
        private String completedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SaleSyncItem {
        private Long id;
        private Long leadId;
        private String leadName;
        private Long projectId;
        private String projectName;
        private String assignedEmployee;
        private String conversionStatus;
        private String conversionDate;
        private BigDecimal dealValue;
        private String notes;
        private String createdAt;
    }
}
