package com.crm.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GoogleSheetsAndAuditControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("Google Sheets & Audit - 401 Unauthorized when unauthenticated")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(post("/api/v1/google-sheets/sync"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /api/v1/google-sheets/sync - Admin triggers sync successfully (200)")
    void testTriggerSyncAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/google-sheets/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.recordsSynced").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("POST /api/v1/google-sheets/sync - Agent gets 403 Forbidden")
    void testTriggerSyncAgentForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/google-sheets/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/v1/google-sheets/status - Admin gets sync status (200)")
    void testGetSyncStatusAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/google-sheets/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/google-sheets/status - Agent gets 403 Forbidden")
    void testGetSyncStatusAgentForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/google-sheets/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/v1/google-sheets/history - Admin gets sync history (200)")
    void testGetSyncHistoryAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/google-sheets/history")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/google-sheets/history - Agent gets 403 Forbidden")
    void testGetSyncHistoryAgentForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/google-sheets/history")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs - Admin gets system audit logs (200)")
    void testGetAuditLogsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs - Agent gets 403 Forbidden")
    void testGetAuditLogsAgentForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
