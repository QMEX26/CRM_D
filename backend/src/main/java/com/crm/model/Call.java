package com.crm.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "calls", indexes = {
    @Index(name = "idx_calls_lead", columnList = "lead_id"),
    @Index(name = "idx_calls_user", columnList = "user_id"),
    @Index(name = "idx_calls_created", columnList = "created_at"),
    @Index(name = "idx_calls_telephony_id", columnList = "telephony_call_id"),
    @Index(name = "idx_calls_lifecycle_status", columnList = "call_lifecycle_status"),
    @Index(name = "idx_calls_final_classification", columnList = "final_classification")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Call {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telephony_call_id", length = 100)
    private String telephonyCallId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lead_id", nullable = true)
    private Lead lead;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "is_connected")
    @Builder.Default
    private Boolean isConnected = false;

    @Column(name = "call_direction", length = 20)
    @Builder.Default
    private String callDirection = "OUTBOUND"; // OUTBOUND, INBOUND

    @Column(name = "call_lifecycle_status", length = 30)
    @Builder.Default
    private String callLifecycleStatus = "ENDED"; // INITIATED, RINGING, CONNECTED, ANSWERED, MISSED, REJECTED, FAILED, CANCELLED, ENDED

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_seconds", nullable = false)
    @Builder.Default
    private Integer durationSeconds = 0;

    @Column(name = "call_status", nullable = false, length = 30)
    private String callStatus; // CONNECTED, MISSED, NO_ANSWER, BUSY, FAILED, REJECTED, CANCELLED

    @Column(name = "business_outcome", length = 30)
    private String businessOutcome; // INTERESTED, NOT_INTERESTED, FOLLOW_UP, WRONG_NUMBER, JUNK, CONVERTED, SALE, OTHER

    @Column(name = "automatic_classification", length = 30)
    private String automaticClassification; // VERY_SHORT, SHORT_CALL, CONNECTED, MISSED, REJECTED, FAILED, CANCELLED

    @Column(name = "final_classification", length = 30)
    private String finalClassification; // VERY_SHORT, SHORT_CALL, INTERESTED, FOLLOW_UP, NOT_INTERESTED, JUNK, SALE, OTHER

    @Column(name = "classification_changed_manually")
    @Builder.Default
    private Boolean classificationChangedManually = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classification_changed_by_user_id")
    private User classificationChangedBy;

    @Column(name = "classification_changed_at")
    private LocalDateTime classificationChangedAt;

    @Column(name = "follow_up_required")
    @Builder.Default
    private Boolean followUpRequired = false;

    @Column(name = "follow_up_date")
    private LocalDateTime followUpDate;

    @Column(name = "follow_up_id")
    private Long followUpId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public String getCallType() {
        return this.callDirection;
    }

    public void setCallType(String callType) {
        this.callDirection = callType;
    }

    public LocalDateTime getCallStartTime() {
        return this.startedAt;
    }

    public LocalDateTime getCallEndTime() {
        return this.endedAt;
    }
}
