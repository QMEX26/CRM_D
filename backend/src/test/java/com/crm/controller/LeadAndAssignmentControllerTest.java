package com.crm.controller;

import com.crm.dto.request.AssignmentRequest;
import com.crm.dto.request.LeadCreateRequest;
import com.crm.dto.request.LeadOutcomeRequest;
import com.crm.dto.request.LeadUpdateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class LeadAndAssignmentControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("GET /api/v1/leads - 401 Unauthorized when not authenticated")
    void testGetLeadsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/leads"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("GET /api/v1/leads - Admin gets all leads paginated (200)")
    void testGetLeadsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/leads")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("GET /api/v1/leads - Agent gets assigned leads with data isolation (200)")
    void testGetLeadsAgentIsolation() throws Exception {
        mockMvc.perform(get("/api/v1/leads")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/leads - Admin creates lead successfully (201)")
    void testCreateLeadAdmin() throws Exception {
        LeadCreateRequest req = new LeadCreateRequest(
                1L, "Rohit Sharma", "+919876543299", "rohit@example.com",
                "Indiranagar", "Bengaluru", "Karnataka", "WEBSITE", "NEW", null, "Interested in 3BHK", null
        );

        mockMvc.perform(post("/api/v1/leads")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Rohit Sharma"))
                .andExpect(jsonPath("$.data.phone").value("+919876543299"));
    }

    @Test
    @DisplayName("POST /api/v1/leads - Agent cannot create lead (403 Forbidden)")
    void testCreateLeadAgentForbidden() throws Exception {
        LeadCreateRequest req = new LeadCreateRequest(
                1L, "Unauthorized Lead", "+919876543298", "agentlead@example.com",
                null, null, null, "CALL", "NEW", null, null, null
        );

        mockMvc.perform(post("/api/v1/leads")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("POST /api/v1/leads - Blank name or phone returns 400 Validation Error")
    void testCreateLeadValidationError() throws Exception {
        LeadCreateRequest req = new LeadCreateRequest(
                1L, "", "", null, null, null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/v1/leads")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isMap());
    }

    @Test
    @DisplayName("POST /api/v1/leads - Duplicate phone in same project returns 409 Conflict")
    void testCreateLeadDuplicatePhone() throws Exception {
        // DataInitializer seeded lead 1 with phone '+91 98111 22334' in project 1
        LeadCreateRequest req = new LeadCreateRequest(
                1L, "Duplicate Contact", "+91 98111 22334", "dup@example.com",
                null, null, null, "REFERRAL", "NEW", null, null, null
        );

        mockMvc.perform(post("/api/v1/leads")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /api/v1/leads/{id} - Admin can view lead details (200)")
    void testGetLeadDetailsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/leads/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Arun Kumar"));
    }

    @Test
    @DisplayName("GET /api/v1/leads/{id} - Non-existent ID returns 404")
    void testGetLeadDetailsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/leads/999999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/v1/leads/{id} - Agent 2 cannot view Agent 1's lead (403 Forbidden IDOR barrier)")
    void testGetLeadDetailsAgentIdorForbidden() throws Exception {
        // Lead 2 is assigned solely to Agent 1. Agent 2 must be blocked with 403 Forbidden.
        mockMvc.perform(get("/api/v1/leads/2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agent2Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("PUT /api/v1/leads/{id} - Admin can update lead (200)")
    void testUpdateLeadAdmin() throws Exception {
        LeadUpdateRequest req = new LeadUpdateRequest(
                "Arun Kumar Updated", "+91 98111 22334", "arun.updated@example.com",
                "Koramangala 5th Block", "Bengaluru", "Karnataka", "WEBSITE", "CONTACTED", "INTERESTED", "Updated info"
        );

        mockMvc.perform(put("/api/v1/leads/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Arun Kumar Updated"));
    }

    @Test
    @DisplayName("PUT /api/v1/leads/{id} - Agent 2 cannot update Agent 1's lead (403 Forbidden IDOR barrier)")
    void testUpdateLeadAgentIdorForbidden() throws Exception {
        // Lead 2 is assigned solely to Agent 1. Agent 2 must be blocked with 403.
        LeadUpdateRequest req = new LeadUpdateRequest(
                "Hacked Name", "+91 98222 33445", "hacked@example.com",
                null, null, null, "WEBSITE", "CONTACTED", null, null
        );

        mockMvc.perform(put("/api/v1/leads/2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agent2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("PATCH /api/v1/leads/{id}/outcome - Assigned Agent 1 can update outcome (200)")
    void testUpdateOutcomeAgentSuccess() throws Exception {
        LeadOutcomeRequest req = new LeadOutcomeRequest("FOLLOW_UP", "FOLLOW_UP");

        mockMvc.perform(patch("/api/v1/leads/1/outcome")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.businessOutcome").value("FOLLOW_UP"));
    }

    @Test
    @DisplayName("PATCH /api/v1/leads/{id}/outcome - Agent 2 cannot update Agent 1's lead outcome (403 Forbidden)")
    void testUpdateOutcomeAgentIdorForbidden() throws Exception {
        // Lead 2 belongs only to Agent 1. Agent 2 must receive 403 Forbidden.
        LeadOutcomeRequest req = new LeadOutcomeRequest("CONVERTED", "CONVERTED");

        mockMvc.perform(patch("/api/v1/leads/2/outcome")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agent2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Lead Assignment & Reassignment Flow with Audit History (200)")
    void testAssignmentAndReassignmentFlow() throws Exception {
        // Step 1: Create a fresh lead as admin
        LeadCreateRequest createReq = new LeadCreateRequest(
                1L, "Assignment Test Lead", "+919988776655", "assign@example.com",
                null, null, null, "WEBSITE", "NEW", null, null, null
        );
        String createRes = mockMvc.perform(post("/api/v1/leads")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(createRes);
        Long leadId = jsonNode.path("data").path("id").asLong();

        // Step 2: Agent cannot assign lead (403)
        AssignmentRequest assignReq1 = new AssignmentRequest(2L);
        mockMvc.perform(post("/api/v1/leads/" + leadId + "/assign")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq1)))
                .andExpect(status().isForbidden());

        // Step 3: Admin assigns lead to Agent 1 (ID 2)
        mockMvc.perform(post("/api/v1/leads/" + leadId + "/assign")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userName").value("Priya Sharma"));

        // Step 4: Admin reassigns lead to Agent 2 (ID 3)
        AssignmentRequest reassignReq = new AssignmentRequest(3L);
        mockMvc.perform(post("/api/v1/leads/" + leadId + "/reassign")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reassignReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userName").value("Kiran Rao"));

        // Step 5: Verify assignment history contains both records
        mockMvc.perform(get("/api/v1/leads/" + leadId + "/assignment-history")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)));

        // Step 6: Delete lead as admin (200)
        mockMvc.perform(delete("/api/v1/leads/" + leadId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Step 7: Delete non-existent lead returns 404
        mockMvc.perform(delete("/api/v1/leads/" + leadId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
