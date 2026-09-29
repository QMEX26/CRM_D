package com.crm.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "leads", uniqueConstraints = {
    @UniqueConstraint(name = "uk_project_phone", columnNames = {"project_id", "phone"})
}, indexes = {
    @Index(name = "idx_leads_project_outcome", columnList = "project_id, business_outcome"),
    @Index(name = "idx_leads_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String source; // Website, Referral, Social Media, Cold Call, etc.

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "NEW"; // NEW, CONTACTED, IN_PROGRESS, FOLLOW_UP, CONVERTED, CLOSED

    @Column(name = "business_outcome", length = 30)
    private String businessOutcome; // INTERESTED, NOT_INTERESTED, FOLLOW_UP, WRONG_NUMBER, JUNK, CONVERTED

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    // Automatic Call Tracking & Metrics
    @Column(name = "last_contacted_at")
    private LocalDateTime lastContactedAt;

    @Column(name = "last_call_id")
    private Long lastCallId;

    @Column(name = "last_call_status", length = 30)
    private String lastCallStatus;

    @Column(name = "last_call_duration")
    private Integer lastCallDuration;

    @Column(name = "total_call_count", nullable = false)
    @Builder.Default
    private Integer totalCallCount = 0;

    @Column(name = "connected_call_count", nullable = false)
    @Builder.Default
    private Integer connectedCallCount = 0;

    @Column(name = "missed_call_count", nullable = false)
    @Builder.Default
    private Integer missedCallCount = 0;

    @Column(name = "rejected_call_count", nullable = false)
    @Builder.Default
    private Integer rejectedCallCount = 0;

    @Column(name = "failed_call_count", nullable = false)
    @Builder.Default
    private Integer failedCallCount = 0;

    @Column(name = "short_call_count", nullable = false)
    @Builder.Default
    private Integer shortCallCount = 0;

    @Column(name = "junk_call_count", nullable = false)
    @Builder.Default
    private Integer junkCallCount = 0;

    @Column(name = "follow_up_required", nullable = false)
    @Builder.Default
    private Boolean followUpRequired = false;

    @Column(name = "next_follow_up_at")
    private LocalDateTime nextFollowUpAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
