package com.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallCreateRequest {
    private Long leadId;
    private String phoneNumber;
    private String callDirection; // OUTBOUND, INBOUND
    private Boolean isConnected;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    private Integer durationSeconds;
    @NotBlank(message = "Call status cannot be blank")
    private String callStatus; // CONNECTED, MISSED, etc. or calculated status
    private String businessOutcome;
    private String notes;

    // Optional immediate follow-up creation
    private boolean createFollowUp;
    private LocalDateTime followUpTime;
    private String followUpNotes;

    public CallCreateRequest(Long leadId, LocalDateTime startedAt, LocalDateTime endedAt,
                             Integer durationSeconds, String callStatus, String businessOutcome,
                             String notes, boolean createFollowUp, LocalDateTime followUpTime,
                             String followUpNotes) {
        this.leadId = leadId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.durationSeconds = durationSeconds;
        this.callStatus = callStatus;
        this.businessOutcome = businessOutcome;
        this.notes = notes;
        this.createFollowUp = createFollowUp;
        this.followUpTime = followUpTime;
        this.followUpNotes = followUpNotes;
    }
}
