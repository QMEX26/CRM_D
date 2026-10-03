package com.crm.specification;

import com.crm.model.AuditLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AuditLogSpecification {

    public static Specification<AuditLog> filterLogs(String entityName, Long userId, String action) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (entityName != null && !entityName.isBlank() && !"ALL".equalsIgnoreCase(entityName)) {
                predicates.add(cb.equal(root.get("entityName"), entityName));
            }

            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }

            if (action != null && !action.isBlank() && !"ALL".equalsIgnoreCase(action)) {
                predicates.add(cb.equal(root.get("action"), action.toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
