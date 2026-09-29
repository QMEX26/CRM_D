package com.crm.controller;

import com.crm.dto.request.ConversionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SalesControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("GET /api/v1/sales/my-sales - 401 Unauthorized when unauthenticated")
    void testGetMySalesUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/sales/my-sales"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("GET /api/v1/sales/my-sales - Agent retrieves their closed sales (200)")
    void testGetMySalesAgent() throws Exception {
        // In DataInitializer, lead 3 was converted by agent (Priya Sharma)
        mockMvc.perform(get("/api/v1/sales/my-sales")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].dealValue").value(35000000.0));
    }

    @Test
    @DisplayName("POST /api/v1/leads/{id}/convert - Assigned Agent converts lead (200)")
    void testConvertLeadAssignedAgent() throws Exception {
        ConversionRequest req = new ConversionRequest(null, new BigDecimal("18500000.00"), "Booking token received");

        mockMvc.perform(post("/api/v1/leads/1/convert")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dealValue").value(18500000.0))
                .andExpect(jsonPath("$.data.leadId").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/sales/convert - Admin converts lead via body leadId (200)")
    void testConvertLeadAdminViaBody() throws Exception {
        // Lead 2 is converted by admin
        ConversionRequest req = new ConversionRequest(2L, new BigDecimal("25000000.00"), "Admin converted VIP lead");

        mockMvc.perform(post("/api/v1/sales/convert")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.leadId").value(2));
    }

    @Test
    @DisplayName("POST /api/v1/leads/{id}/convert - Non-existent lead returns 404")
    void testConvertLeadNotFound() throws Exception {
        ConversionRequest req = new ConversionRequest(null, new BigDecimal("1000000.00"), "Notes");

        mockMvc.perform(post("/api/v1/leads/999999/convert")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/v1/leads/{id}/convert - Unassigned Agent 2 gets 403 Forbidden (IDOR barrier)")
    void testConvertLeadAgentIdorForbidden() throws Exception {
        // Lead 2 belongs only to Agent 1. Agent 2 must be blocked with 403.
        ConversionRequest req = new ConversionRequest(null, new BigDecimal("15000000.00"), "Unauthorized conversion attempt");

        mockMvc.perform(post("/api/v1/leads/2/convert")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agent2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("POST /api/v1/sales/convert - Missing leadId returns 400 Bad Request")
    void testConvertLeadMissingId() throws Exception {
        ConversionRequest req = new ConversionRequest(null, new BigDecimal("500000.00"), "Notes without leadId");

        mockMvc.perform(post("/api/v1/sales/convert")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
