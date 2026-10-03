package com.crm.specification;

import com.crm.model.Call;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CallSpecification {

    public static Specification<Call> filterCalls(Long userId, Long leadId, Long projectId, String status, String outcome,
                                                  LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (leadId != null) {
                predicates.add(cb.equal(root.get("lead").get("id"), leadId));
            }
            if (projectId != null) {
                predicates.add(cb.equal(root.join("lead", JoinType.LEFT).get("project").get("id"), projectId));
            }
            if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                if ("CONNECTED".equalsIgnoreCase(status)) {
                    predicates.add(cb.or(
                            cb.equal(root.get("callStatus"), "CONNECTED"),
                            cb.isTrue(root.get("isConnected")),
                            cb.greaterThan(root.get("durationSeconds"), 0)
                    ));
                } else {
                    predicates.add(cb.equal(root.get("callStatus"), status.toUpperCase()));
                }
            }
            if (outcome != null && !outcome.isBlank() && !"ALL".equalsIgnoreCase(outcome)) {
                predicates.add(cb.equal(root.get("businessOutcome"), outcome));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
