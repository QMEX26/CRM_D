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
public class GoogleSheetsSyncResponse {
    private Long syncId;
    private String syncCode;
    private String status; // IN_PROGRESS, SUCCESS, FAILED
    private int recordsSynced;
    private String message;
    private String triggeredBy;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private SyncSummary summary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SyncSummary {
        private int users;
        private int projects;
        private int leads;
        private int assignments;
        private int calls;
        private int followUps;
        private int sales;
    }
}
