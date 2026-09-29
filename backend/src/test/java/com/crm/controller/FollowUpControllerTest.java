package com.crm.controller;

import com.crm.dto.request.FollowUpCreateRequest;
import com.crm.dto.request.FollowUpStatusRequest;
import com.crm.dto.request.FollowUpUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FollowUpControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("GET /api/v1/follow-ups - 401 Unauthorized when unauthenticated")
    void testGetFollowUpsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/follow-ups"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("GET /api/v1/follow-ups - Admin gets all follow-ups (200)")
    void testGetFollowUpsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/follow-ups")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("GET /api/v1/follow-ups/today - Retrieves today's follow-ups (200)")
    void testGetTodayFollowUps() throws Exception {
        // DataInitializer seeded a follow-up for today on lead 1 for agent 1
        mockMvc.perform(get("/api/v1/follow-ups/today")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/follow-ups/upcoming - Retrieves upcoming follow-ups (200)")
    void testGetUpcomingFollowUps() throws Exception {
        mockMvc.perform(get("/api/v1/follow-ups/upcoming")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/follow-ups/overdue - Retrieves overdue follow-ups (200)")
    void testGetOverdueFollowUps() throws Exception {
        // DataInitializer seeded an overdue follow-up on lead 2 for agent 1
        mockMvc.perform(get("/api/v1/follow-ups/overdue")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/follow-ups - Assigned Agent schedules follow-up (201)")
    void testCreateFollowUpAssignedAgent() throws Exception {
        FollowUpCreateRequest req = new FollowUpCreateRequest(
                1L, LocalDateTime.now().plusDays(2), "Discuss updated pricing sheet"
        );

        mockMvc.perform(post("/api/v1/follow-ups")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.leadId").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/follow-ups - Unassigned Agent 2 gets 403 Forbidden (IDOR barrier)")
    void testCreateFollowUpAgentIdorForbidden() throws Exception {
        // Lead 2 belongs only to Agent 1. Agent 2 must be blocked with 403.
        FollowUpCreateRequest req = new FollowUpCreateRequest(
                2L, LocalDateTime.now().plusDays(1), "Unauthorized follow-up"
        );

        mockMvc.perform(post("/api/v1/follow-ups")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agent2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("PUT /api/v1/follow-ups/{id} - Assigned Agent updates own follow-up (200)")
    void testUpdateFollowUpAgentSuccess() throws Exception {
        FollowUpUpdateRequest req = new FollowUpUpdateRequest(
                LocalDateTime.now().plusDays(3), "PENDING", "Rescheduled due to client request"
        );

        mockMvc.perform(put("/api/v1/follow-ups/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notes").value("Rescheduled due to client request"));
    }

    @Test
    @DisplayName("PUT /api/v1/follow-ups/{id} - Agent 2 cannot update Agent 1's follow-up (403 Forbidden IDOR barrier)")
    void testUpdateFollowUpAgentIdorForbidden() throws Exception {
        // Follow-up 1 belongs to Agent 1 (ID 2). Agent 2 (ID 3) must be blocked with 403.
        FollowUpUpdateRequest req = new FollowUpUpdateRequest(
                LocalDateTime.now().plusDays(5), "CANCELLED", "Hacked update"
        );

        mockMvc.perform(put("/api/v1/follow-ups/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agent2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("PATCH /api/v1/follow-ups/{id}/status - Agent completes own follow-up (200)")
    void testToggleStatusSuccess() throws Exception {
        FollowUpStatusRequest req = new FollowUpStatusRequest("COMPLETED");

        mockMvc.perform(patch("/api/v1/follow-ups/1/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("PATCH /api/v1/follow-ups/{id}/status - Agent 2 cannot toggle Agent 1's follow-up (403 Forbidden)")
    void testToggleStatusAgentIdorForbidden() throws Exception {
        FollowUpStatusRequest req = new FollowUpStatusRequest("CANCELLED");

        mockMvc.perform(patch("/api/v1/follow-ups/1/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agent2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
