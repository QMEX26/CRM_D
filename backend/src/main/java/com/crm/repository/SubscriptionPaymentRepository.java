package com.crm.repository;

import com.crm.model.SubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Long> {
    Optional<SubscriptionPayment> findFirstByRazorpayOrderIdOrderByCreatedAtDesc(String razorpayOrderId);
    Optional<SubscriptionPayment> findFirstByRazorpayPaymentIdOrderByCreatedAtDesc(String razorpayPaymentId);
    List<SubscriptionPayment> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<SubscriptionPayment> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
