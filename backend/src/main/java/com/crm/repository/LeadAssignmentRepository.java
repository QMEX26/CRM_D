package com.crm.repository;

import com.crm.model.LeadAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadAssignmentRepository extends JpaRepository<LeadAssignment, Long> {
    Optional<LeadAssignment> findFirstByLeadIdAndIsActiveTrueOrderByAssignedAtDesc(Long leadId);

    default Optional<LeadAssignment> findByLeadIdAndIsActiveTrue(Long leadId) {
        return findFirstByLeadIdAndIsActiveTrueOrderByAssignedAtDesc(leadId);
    }

    List<LeadAssignment> findByLeadIdOrderByAssignedAtDesc(Long leadId);

    List<LeadAssignment> findByUserIdAndIsActiveTrue(Long userId);

    boolean existsByLeadIdAndUserIdAndIsActiveTrue(Long leadId, Long userId);

    boolean existsByLeadIdAndUserId(Long leadId, Long userId);

    long countByUserIdAndIsActiveTrue(Long userId);

    List<LeadAssignment> findByUserId(Long userId);

    List<LeadAssignment> findByAssignedById(Long assignedById);

    @Query("SELECT a.user.id, COUNT(a) FROM LeadAssignment a WHERE a.isActive = true GROUP BY a.user.id")
    List<Object[]> countActiveWorkloadByUser();
}
