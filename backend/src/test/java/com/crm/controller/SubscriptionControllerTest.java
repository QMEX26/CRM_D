package com.crm.controller;

import com.crm.dto.request.CreateSubscriptionOrderRequest;
import com.crm.dto.request.LoginRequest;
import com.crm.dto.request.VerifyPaymentRequest;
import com.crm.model.SubscriptionPlan;
import com.crm.repository.SubscriptionPaymentRepository;
import com.crm.repository.SubscriptionPlanRepository;
import com.crm.repository.SubscriptionRepository;
import com.crm.service.RazorpayService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPaymentRepository subscriptionPaymentRepository;

    @MockBean
    private RazorpayService razorpayService;

    private String adminToken;
    private String agentToken;

    @BeforeEach
    void setUp() throws Exception {
        when(razorpayService.getKeyId()).thenReturn("rzp_test_placeholder");
        when(razorpayService.createOrder(anyLong(), anyString(), anyString(), anyMap()))
                .thenReturn("order_test_123456");
        when(razorpayService.verifyPaymentSignature(eq("order_test_123456"), eq("pay_test_123456"), eq("valid_test_signature")))
                .thenReturn(true);
        when(razorpayService.verifyPaymentSignature(eq("order_test_123456"), eq("pay_test_123456"), eq("invalid_sig")))
                .thenReturn(false);

        // Ensure plans exist
        subscriptionPlanRepository.findByName("ADMIN_MONTHLY").orElseGet(() ->
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

        subscriptionPlanRepository.findByName("USER_MONTHLY").ifPresentOrElse(
                plan -> {
                    plan.setPrice(new BigDecimal("99.00"));
                    subscriptionPlanRepository.save(plan);
                },
                () -> subscriptionPlanRepository.save(SubscriptionPlan.builder()
                        .name("USER_MONTHLY")
                        .targetRole("ROLE_USER")
                        .price(new BigDecimal("99.00"))
                        .currency("INR")
                        .billingCycle("MONTHLY")
                        .trialDays(7)
                        .active(true)
                        .build())
        );

        // Obtain Admin Token
        LoginRequest adminLogin = new LoginRequest("admin@crm.com", "admin123");
        MvcResult adminRes = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode adminJson = objectMapper.readTree(adminRes.getResponse().getContentAsString());
        adminToken = adminJson.path("data").path("token").asText();

        // Obtain Agent Token
        LoginRequest agentLogin = new LoginRequest("agent@crm.com", "agent123");
        MvcResult agentRes = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agentLogin)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode agentJson = objectMapper.readTree(agentRes.getResponse().getContentAsString());
        agentToken = agentJson.path("data").path("token").asText();
    }

    @Test
    @DisplayName("GET /api/v1/subscription/plans - Returns active plans")
    void testGetActivePlans() throws Exception {
        mockMvc.perform(get("/api/v1/subscription/plans")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/subscription/plan - Admin gets ₹299 plan")
    void testGetAdminPlan() throws Exception {
        mockMvc.perform(get("/api/v1/subscription/plan")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("ADMIN_MONTHLY"))
                .andExpect(jsonPath("$.data.price").value(299.00));
    }

    @Test
    @DisplayName("GET /api/v1/subscription/plan - Agent User gets ₹99 plan")
    void testGetAgentPlan() throws Exception {
        mockMvc.perform(get("/api/v1/subscription/plan")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("USER_MONTHLY"))
                .andExpect(jsonPath("$.data.price").value(99.00));
    }

    @Test
    @DisplayName("POST /api/v1/subscription/create-order - Creates Razorpay order for Admin (₹299)")
    void testCreateOrderAdmin() throws Exception {
        CreateSubscriptionOrderRequest request = new CreateSubscriptionOrderRequest();

        mockMvc.perform(post("/api/v1/subscription/create-order")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value("order_test_123456"))
                .andExpect(jsonPath("$.data.amountInPaise").value(29900))
                .andExpect(jsonPath("$.data.amount").value(299.00));
    }

    @Test
    @DisplayName("POST /api/v1/subscription/create-order - Creates Razorpay order for Agent User (₹99)")
    void testCreateOrderAgent() throws Exception {
        CreateSubscriptionOrderRequest request = new CreateSubscriptionOrderRequest();

        mockMvc.perform(post("/api/v1/subscription/create-order")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value("order_test_123456"))
                .andExpect(jsonPath("$.data.amountInPaise").value(9900))
                .andExpect(jsonPath("$.data.amount").value(99.00));
    }

    @Test
    @DisplayName("POST /api/v1/subscription/verify-payment - Activates subscription on valid signature")
    void testVerifyPaymentSuccess() throws Exception {
        // First create order
        mockMvc.perform(post("/api/v1/subscription/create-order")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSubscriptionOrderRequest())))
                .andExpect(status().isCreated());

        // Then verify payment
        VerifyPaymentRequest verifyReq = new VerifyPaymentRequest("order_test_123456", "pay_test_123456", "valid_test_signature");

        mockMvc.perform(post("/api/v1/subscription/verify-payment")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/subscription/verify-payment - Fails on invalid signature")
    void testVerifyPaymentInvalidSignature() throws Exception {
        // First create order
        mockMvc.perform(post("/api/v1/subscription/create-order")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSubscriptionOrderRequest())))
                .andExpect(status().isCreated());

        // Attempt verification with bad signature
        VerifyPaymentRequest verifyReq = new VerifyPaymentRequest("order_test_123456", "pay_test_123456", "invalid_sig");

        mockMvc.perform(post("/api/v1/subscription/verify-payment")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Payment verification failed: Invalid signature"));
    }

    @Test
    @DisplayName("GET /api/v1/subscription/my - Unauthenticated request returns 401")
    void testUnauthenticatedGetMySubscription() throws Exception {
        mockMvc.perform(get("/api/v1/subscription/my"))
                .andExpect(status().isUnauthorized());
    }
}
