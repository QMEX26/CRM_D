package com.crm.repository;

import com.crm.model.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    boolean existsByProjectIdAndPhone(Long projectId, String phone);

    Optional<Lead> findByProjectIdAndPhone(Long projectId, String phone);

    Optional<Lead> findFirstByPhoneOrderByCreatedAtDesc(String phone);

    List<Lead> findByProjectId(Long projectId);

    @Query("SELECT l FROM Lead l WHERE " +
           "(:projectId IS NULL OR l.project.id = :projectId) AND " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:outcome IS NULL OR l.businessOutcome = :outcome) AND " +
           "(:search IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "  OR l.phone LIKE CONCAT('%', :search, '%') " +
           "  OR LOWER(l.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Lead> searchLeads(@Param("projectId") Long projectId,
                           @Param("status") String status,
                           @Param("outcome") String outcome,
                           @Param("search") String search,
                           Pageable pageable);

    @Query("SELECT l FROM Lead l WHERE l.id IN " +
           "(SELECT a.lead.id FROM LeadAssignment a WHERE a.user.id = :userId AND a.isActive = true) AND " +
           "(:projectId IS NULL OR l.project.id = :projectId) AND " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:outcome IS NULL OR l.businessOutcome = :outcome) AND " +
           "(:search IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "  OR l.phone LIKE CONCAT('%', :search, '%') " +
           "  OR LOWER(l.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Lead> searchAssignedLeads(@Param("userId") Long userId,
                                   @Param("projectId") Long projectId,
                                   @Param("status") String status,
                                   @Param("outcome") String outcome,
                                   @Param("search") String search,
                                   Pageable pageable);

    long countByProjectId(Long projectId);
    long countByStatus(String status);
    long countByBusinessOutcome(String businessOutcome);

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.id IN (SELECT a.lead.id FROM LeadAssignment a WHERE a.isActive = true)")
    long countAssignedLeads();

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.id NOT IN (SELECT a.lead.id FROM LeadAssignment a WHERE a.isActive = true)")
    long countUnassignedLeads();

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.id IN " +
           "(SELECT a.lead.id FROM LeadAssignment a WHERE a.user.id = :userId AND a.isActive = true)")
    long countAssignedToUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.project.id = :projectId AND l.id IN " +
           "(SELECT a.lead.id FROM LeadAssignment a WHERE a.user.id = :userId AND a.isActive = true)")
    long countAssignedToUserInProject(@Param("userId") Long userId, @Param("projectId") Long projectId);

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.businessOutcome = :outcome AND l.id IN " +
           "(SELECT a.lead.id FROM LeadAssignment a WHERE a.user.id = :userId AND a.isActive = true)")
    long countOutcomeForUser(@Param("userId") Long userId, @Param("outcome") String outcome);

    @Query("SELECT l FROM Lead l WHERE l.id IN " +
           "(SELECT a.lead.id FROM LeadAssignment a WHERE a.user.id = :userId AND a.isActive = true)")
    List<Lead> findAllAssignedToUser(@Param("userId") Long userId);
}
