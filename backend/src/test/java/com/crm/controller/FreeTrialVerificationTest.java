package com.crm.controller;

import com.crm.dto.request.VerifyPaymentRequest;
import com.crm.dto.response.SubscriptionResponse;
import com.crm.model.*;
import com.crm.repository.*;
import com.crm.service.RazorpayService;
import com.crm.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FreeTrialVerificationTest {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private SubscriptionPaymentRepository subscriptionPaymentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private RazorpayService razorpayService;

    private Role userRole;
    private Role adminRole;
    private SubscriptionPlan userPlan;
    private SubscriptionPlan adminPlan;

    @BeforeEach
    void setup() {
        adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
        userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        adminPlan = subscriptionPlanRepository.findByName("ADMIN_MONTHLY").orElseGet(() ->
                subscriptionPlanRepository.save(SubscriptionPlan.builder()
                        .name("ADMIN_MONTHLY")
                        .targetRole("ROLE_ADMIN")
                        .price(new BigDecimal("299.00"))
                        .currency("INR")
                        .billingCycle("MONTHLY")
                        .trialDays(7)
                        .active(true)
                        .build())
        );

        userPlan = subscriptionPlanRepository.findByName("USER_MONTHLY").map(plan -> {
            plan.setPrice(new BigDecimal("99.00"));
            return subscriptionPlanRepository.save(plan);
        }).orElseGet(() ->
                subscriptionPlanRepository.save(SubscriptionPlan.builder()
                        .name("USER_MONTHLY")
                        .targetRole("ROLE_USER")
                        .price(new BigDecimal("99.00"))
                        .currency("INR")
                        .billingCycle("MONTHLY")
                        .trialDays(7)
                        .active(true)
                        .build())
        );
    }

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private User createUserWithCustomCreatedAt(String email, LocalDateTime createdAt, Role role) {
        User user = User.builder()
                .name("Test " + email)
                .email(email)
                .phone("+91 99999 " + (System.currentTimeMillis() % 90000 + 10000))
                .password(passwordEncoder.encode("password123"))
                .role(role)
                .status("ACTIVE")
                .build();
        User saved = userRepository.saveAndFlush(user);

        // Update created_at in database and clear EntityManager cache
        entityManager.createNativeQuery("UPDATE users SET created_at = :cat WHERE id = :id")
                .setParameter("cat", java.sql.Timestamp.valueOf(createdAt))
                .setParameter("id", saved.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        return userRepository.findById(saved.getId()).orElseThrow();
    }

    @Test
    @DisplayName("Case 1: User created today -> 7 yellow blocks & 7 DAYS REMAINING")
    void testUserCreatedToday() {
        LocalDateTime now = LocalDateTime.now();
        User user = createUserWithCustomCreatedAt("trial_today@test.com", now, userRole);

        SubscriptionResponse sub = subscriptionService.getSubscriptionForUser(user.getId());

        assertNotNull(sub);
        assertEquals("FREE_TRIAL", sub.getStatus());
        assertTrue(sub.getIsTrialActive());
        assertTrue(sub.getIsTrial());
        assertTrue(sub.getIsActive());
        assertFalse(sub.getIsExpired());
        assertEquals(7L, sub.getTrialDaysRemaining());
        assertEquals(7L, sub.getDaysRemaining());
        assertNotNull(sub.getTrialStartDate());
        assertNotNull(sub.getTrialEndDate());
    }

    @Test
    @DisplayName("Case 2: User created 1 day ago -> 6 yellow blocks & 6 DAYS REMAINING")
    void testUserCreated1DayAgo() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1).minusMinutes(10);
        User user = createUserWithCustomCreatedAt("trial_1day@test.com", createdAt, userRole);

        SubscriptionResponse sub = subscriptionService.getSubscriptionForUser(user.getId());

        assertNotNull(sub);
        assertEquals("FREE_TRIAL", sub.getStatus());
        assertTrue(sub.getIsTrialActive());
        assertEquals(6L, sub.getTrialDaysRemaining());
        assertEquals(6L, sub.getDaysRemaining());
    }

    @Test
    @DisplayName("Case 3: User created 2 days ago -> 5 yellow blocks & 5 DAYS REMAINING")
    void testUserCreated2DaysAgo() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(2).minusMinutes(10);
        User user = createUserWithCustomCreatedAt("trial_2days@test.com", createdAt, userRole);

        SubscriptionResponse sub = subscriptionService.getSubscriptionForUser(user.getId());

        assertNotNull(sub);
        assertEquals("FREE_TRIAL", sub.getStatus());
        assertTrue(sub.getIsTrialActive());
        assertEquals(5L, sub.getTrialDaysRemaining());
        assertEquals(5L, sub.getDaysRemaining());
    }

    @Test
    @DisplayName("Case 4: User created 6 days ago -> 1 yellow block & 1 DAY REMAINING")
    void testUserCreated6DaysAgo() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(6).minusMinutes(10);
        User user = createUserWithCustomCreatedAt("trial_6days@test.com", createdAt, userRole);

        SubscriptionResponse sub = subscriptionService.getSubscriptionForUser(user.getId());

        assertNotNull(sub);
        assertEquals("FREE_TRIAL", sub.getStatus());
        assertTrue(sub.getIsTrialActive());
        assertEquals(1L, sub.getTrialDaysRemaining());
        assertEquals(1L, sub.getDaysRemaining());
    }

    @Test
    @DisplayName("Case 5: User created >7 days ago -> 0 yellow blocks & FREE TRIAL EXPIRED")
    void testUserCreatedMoreThan7DaysAgo() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(8);
        User user = createUserWithCustomCreatedAt("trial_expired@test.com", createdAt, userRole);

        SubscriptionResponse sub = subscriptionService.getSubscriptionForUser(user.getId());

        assertNotNull(sub);
        assertEquals("EXPIRED", sub.getStatus());
        assertFalse(sub.getIsTrialActive());
        assertFalse(sub.getIsActive());
        assertTrue(sub.getIsExpired());
        assertEquals(0L, sub.getTrialDaysRemaining());
        assertEquals(0L, sub.getDaysRemaining());
    }

    @Test
    @DisplayName("Case 6: Two users created on different dates have distinct trial countdowns")
    void testTwoUsersDifferentDates() {
        LocalDateTime dateA = LocalDateTime.now().minusDays(2);
        LocalDateTime dateB = LocalDateTime.now().minusDays(4);

        User userA = createUserWithCustomCreatedAt("user_a@test.com", dateA, userRole);
        User userB = createUserWithCustomCreatedAt("user_b@test.com", dateB, userRole);

        SubscriptionResponse subA = subscriptionService.getSubscriptionForUser(userA.getId());
        SubscriptionResponse subB = subscriptionService.getSubscriptionForUser(userB.getId());

        assertEquals(5L, subA.getTrialDaysRemaining());
        assertEquals(3L, subB.getTrialDaysRemaining());
        assertNotEquals(subA.getTrialEndDate(), subB.getTrialEndDate());
    }

    @Test
    @DisplayName("Cases 7-9: Multiple logins/restarts do NOT reset trial (immutable created_at)")
    void testTrialPersistenceAcrossSessions() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(3);
        User user = createUserWithCustomCreatedAt("persistent_user@test.com", createdAt, userRole);

        // First login / session check
        SubscriptionResponse session1 = subscriptionService.getSubscriptionForUser(user.getId());
        assertEquals(4L, session1.getTrialDaysRemaining());

        // Simulated logout / app restart / fresh re-fetch
        SubscriptionResponse session2 = subscriptionService.getSubscriptionForUser(user.getId());
        assertEquals(4L, session2.getTrialDaysRemaining());
        assertEquals(session1.getTrialStartDate(), session2.getTrialStartDate());
        assertEquals(session1.getTrialEndDate(), session2.getTrialEndDate());
    }

    @Test
    @DisplayName("Case 10: Existing paid subscriber retains PAID status with full priority over trial")
    void testPaidSubscriberPriority() {
        User user = createUserWithCustomCreatedAt("paid_user@test.com", LocalDateTime.now().minusDays(1), userRole);

        // Add active paid subscription
        Subscription paidSub = Subscription.builder()
                .user(user)
                .plan(userPlan)
                .status(SubscriptionStatus.ACTIVE)
                .currentPeriodStart(LocalDateTime.now())
                .currentPeriodEnd(LocalDateTime.now().plusMonths(1))
                .build();
        subscriptionRepository.save(paidSub);

        SubscriptionResponse sub = subscriptionService.getSubscriptionForUser(user.getId());

        assertEquals("ACTIVE", sub.getStatus());
        assertEquals("PAID", sub.getSubscriptionType());
        assertTrue(sub.getIsActive());
        assertFalse(sub.getIsTrialActive());
        assertFalse(sub.getIsTrial());
        assertTrue(sub.getDaysRemaining() >= 28);
    }

    @Test
    @DisplayName("Case 11: Admin user maintains admin plan and role integrity")
    void testAdminUserIntegrity() {
        User admin = createUserWithCustomCreatedAt("admin_trial@test.com", LocalDateTime.now(), adminRole);

        SubscriptionResponse sub = subscriptionService.getSubscriptionForUser(admin.getId());

        assertNotNull(sub);
        assertEquals("ROLE_ADMIN", sub.getUserRole());
        assertEquals("ADMIN_MONTHLY", sub.getPlan().getName());
        assertEquals(new BigDecimal("299.00"), sub.getPlan().getPrice());
    }
}
