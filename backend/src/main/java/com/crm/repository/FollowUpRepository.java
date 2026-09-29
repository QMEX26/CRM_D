package com.crm.repository;

import com.crm.model.FollowUp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {
    List<FollowUp> findByLeadIdOrderByScheduledTimeDesc(Long leadId);

    @Query("SELECT f FROM FollowUp f WHERE " +
           "(:userId IS NULL OR f.user.id = :userId) AND " +
           "(:status IS NULL OR f.status = :status) AND " +
           "(:startTime IS NULL OR f.scheduledTime >= :startTime) AND " +
           "(:endTime IS NULL OR f.scheduledTime <= :endTime) " +
           "ORDER BY f.scheduledTime ASC")
    Page<FollowUp> searchFollowUps(@Param("userId") Long userId,
                                   @Param("status") String status,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime,
                                   Pageable pageable);

    // Overdue: status == 'PENDING' and scheduledTime < now
    @Query("SELECT f FROM FollowUp f WHERE " +
           "(:userId IS NULL OR f.user.id = :userId) AND " +
           "f.status = 'PENDING' AND f.scheduledTime < :now " +
           "ORDER BY f.scheduledTime ASC")
    List<FollowUp> findOverdueFollowUps(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Today: status == 'PENDING' and scheduledTime between startOfDay and endOfDay
    @Query("SELECT f FROM FollowUp f WHERE " +
           "(:userId IS NULL OR f.user.id = :userId) AND " +
           "f.status = 'PENDING' AND f.scheduledTime BETWEEN :startOfDay AND :endOfDay " +
           "ORDER BY f.scheduledTime ASC")
    List<FollowUp> findTodayFollowUps(@Param("userId") Long userId,
                                      @Param("startOfDay") LocalDateTime startOfDay,
                                      @Param("endOfDay") LocalDateTime endOfDay);

    // Upcoming: status == 'PENDING' and scheduledTime > endOfDay
    @Query("SELECT f FROM FollowUp f WHERE " +
           "(:userId IS NULL OR f.user.id = :userId) AND " +
           "f.status = 'PENDING' AND f.scheduledTime > :endOfDay " +
           "ORDER BY f.scheduledTime ASC")
    List<FollowUp> findUpcomingFollowUps(@Param("userId") Long userId, @Param("endOfDay") LocalDateTime endOfDay);

    long countByUserIdAndStatusAndScheduledTimeLessThan(Long userId, String status, LocalDateTime now);
    long countByUserIdAndStatusAndScheduledTimeGreaterThanEqual(Long userId, String status, LocalDateTime now);
    long countByUserIdAndStatusAndScheduledTimeBetween(Long userId, String status, LocalDateTime start, LocalDateTime end);
    long countByStatus(String status);
    long countByStatusAndScheduledTimeLessThan(String status, LocalDateTime now);

    List<FollowUp> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
