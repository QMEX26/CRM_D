package com.crm.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "google_sheets_sync_logs", indexes = {
    @Index(name = "idx_sync_status", columnList = "status"),
    @Index(name = "idx_sync_started", columnList = "started_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleSheetsSyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "triggered_by", nullable = false)
    private User triggeredBy;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "IN_PROGRESS"; // IN_PROGRESS, SUCCESS, FAILED

    @Column(name = "sync_id", length = 50)
    private String syncId;

    @Column(name = "records_synced")
    @Builder.Default
    private Integer recordsSynced = 0;

    @Column(name = "users_count")
    @Builder.Default
    private Integer usersCount = 0;

    @Column(name = "projects_count")
    @Builder.Default
    private Integer projectsCount = 0;

    @Column(name = "leads_count")
    @Builder.Default
    private Integer leadsCount = 0;

    @Column(name = "assignments_count")
    @Builder.Default
    private Integer assignmentsCount = 0;

    @Column(name = "calls_count")
    @Builder.Default
    private Integer callsCount = 0;

    @Column(name = "followups_count")
    @Builder.Default
    private Integer followupsCount = 0;

    @Column(name = "sales_count")
    @Builder.Default
    private Integer salesCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
