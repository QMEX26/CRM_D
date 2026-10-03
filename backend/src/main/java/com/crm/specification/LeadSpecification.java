package com.crm.specification;

import com.crm.model.Lead;
import com.crm.model.LeadAssignment;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class LeadSpecification {

    public static Specification<Lead> filterLeads(Long userId, Long projectId, String status, String outcome, String search, boolean assignedOnly) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (assignedOnly && userId != null) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<LeadAssignment> assignmentRoot = subquery.from(LeadAssignment.class);
                subquery.select(assignmentRoot.get("lead").get("id"))
                        .where(
                                cb.equal(assignmentRoot.get("user").get("id"), userId),
                                cb.isTrue(assignmentRoot.get("isActive"))
                        );
                predicates.add(root.get("id").in(subquery));
            }

            if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }

            if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                predicates.add(cb.equal(root.get("status"), status.toUpperCase()));
            }

            if (outcome != null && !outcome.isBlank() && !"ALL".equalsIgnoreCase(outcome)) {
                predicates.add(cb.equal(root.get("businessOutcome"), outcome));
            }

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase().trim() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
                Predicate phoneLike = cb.like(root.get("phone"), "%" + search.trim() + "%");
                Predicate emailLike = cb.like(cb.lower(root.get("email")), pattern);
                predicates.add(cb.or(nameLike, phoneLike, emailLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
