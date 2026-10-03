package com.crm.repository;

import com.crm.model.Subscription;
import com.crm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUser(User user);
    Optional<Subscription> findByUserId(Long userId);
    Optional<Subscription> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByUserId(Long userId);
}
