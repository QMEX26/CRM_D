package com.crm.repository;

import com.crm.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesRepository extends JpaRepository<Sale, Long> {
    Optional<Sale> findByLeadId(Long leadId);
    List<Sale> findByUserIdOrderByConvertedAtDesc(Long userId);
    List<Sale> findAllByOrderByConvertedAtDesc();

    long countByUserId(Long userId);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.lead.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(SUM(s.dealValue), 0) FROM Sale s")
    BigDecimal sumTotalDealValue();

    @Query("SELECT COALESCE(SUM(s.dealValue), 0) FROM Sale s WHERE s.user.id = :userId")
    BigDecimal sumDealValueByUserId(@Param("userId") Long userId);

    long countByConvertedAtBetween(LocalDateTime start, LocalDateTime end);
}
