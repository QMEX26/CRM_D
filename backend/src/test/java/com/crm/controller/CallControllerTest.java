package com.crm.controller;

import com.crm.dto.request.CallCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CallControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("GET /api/v1/calls - 401 Unauthorized when unauthenticated")
    void testGetCallsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/calls"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("GET /api/v1/calls - Admin gets paginated call logs (200)")
    void testGetCallsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/calls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("GET /api/v1/calls - Agent gets own calls with data isolation (200)")
    void testGetCallsAgentIsolation() throws Exception {
        mockMvc.perform(get("/api/v1/calls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/calls - Admin logs call successfully (201)")
    void testLogCallAdmin() throws Exception {
        CallCreateRequest req = new CallCreateRequest(
                1L, LocalDateTime.now().minusMinutes(5), LocalDateTime.now(), 300,
                "CONNECTED", "INTERESTED", "Good conversation about 3BHK", false, null, null
        );

        mockMvc.perform(post("/api/v1/calls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.callStatus").value("CONNECTED"))
                .andExpect(jsonPath("$.data.businessOutcome").value("INTERESTED"))
                .andExpect(jsonPath("$.data.durationSeconds").value(300));
    }

    @Test
    @DisplayName("POST /api/v1/calls - Assigned Agent logs call with outcome update (201)")
    void testLogCallAssignedAgent() throws Exception {
        CallCreateRequest req = new CallCreateRequest(
                1L, LocalDateTime.now().minusMinutes(2), LocalDateTime.now(), 120,
                "CONNECTED", "FOLLOW_UP", "Customer requested callback tomorrow", false, null, null
        );

        mockMvc.perform(post("/api/v1/calls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.leadId").value(1))
                .andExpect(jsonPath("$.data.callStatus").value("CONNECTED"));
    }

    @Test
    @DisplayName("POST /api/v1/calls - Missing required fields returns 400 Validation Error")
    void testLogCallValidationError() throws Exception {
        CallCreateRequest req = new CallCreateRequest(
                null, null, null, null, "", null, null, false, null, null
        );

        mockMvc.perform(post("/api/v1/calls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isMap());
    }

    @Test
    @DisplayName("POST /api/v1/calls - Non-existent lead returns 404")
    void testLogCallNotFound() throws Exception {
        CallCreateRequest req = new CallCreateRequest(
                999999L, null, null, 60, "CONNECTED", "INTERESTED", "Test", false, null, null
        );

        mockMvc.perform(post("/api/v1/calls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/v1/calls - Unassigned Agent 2 gets 403 Forbidden (IDOR barrier)")
    void testLogCallAgentIdorForbidden() throws Exception {
        // Lead 2 belongs only to Agent 1. Agent 2 must be blocked with 403.
        CallCreateRequest req = new CallCreateRequest(
                2L, null, null, 90, "CONNECTED", "INTERESTED", "Unauthorized call log", false, null, null
        );

        mockMvc.perform(post("/api/v1/calls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agent2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/v1/leads/{id}/calls - Admin retrieves call history for lead (200)")
    void testGetCallsForLeadAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/leads/1/calls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/v1/leads/{id}/calls - Non-existent lead returns 404")
    void testGetCallsForLeadNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/leads/999999/calls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/v1/leads/{id}/calls - Unassigned Agent 2 gets 403 Forbidden (IDOR barrier)")
    void testGetCallsForLeadAgentIdorForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/leads/2/calls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agent2Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/v1/leads/{id}/call-stats - Admin retrieves call stats (200)")
    void testGetCallStatsForLeadAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/leads/1/call-stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCalls").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.totalDurationSeconds").value(greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("GET /api/v1/leads/{id}/call-stats - Non-existent lead returns 404")
    void testGetCallStatsForLeadNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/leads/999999/call-stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/v1/leads/{id}/call-stats - Unassigned Agent 2 gets 403 Forbidden (IDOR barrier)")
    void testGetCallStatsForLeadAgentIdorForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/leads/2/call-stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agent2Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
