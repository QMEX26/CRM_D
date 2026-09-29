package com.crm.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance", uniqueConstraints = {
    @UniqueConstraint(name = "uk_attendance_user_date", columnNames = {"user_id", "attendance_date"})
}, indexes = {
    @Index(name = "idx_attendance_user", columnList = "user_id"),
    @Index(name = "idx_attendance_date", columnList = "attendance_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate date;

    @Column(name = "clock_in_time")
    private LocalDateTime clockInTime;

    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;

    @Column(name = "duration_minutes")
    @Builder.Default
    private Integer durationMinutes = 0;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PRESENT"; // PRESENT, HALF_DAY, LEAVE, HOLIDAY

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
