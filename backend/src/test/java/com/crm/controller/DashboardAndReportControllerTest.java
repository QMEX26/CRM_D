package com.crm.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DashboardAndReportControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("GET /api/v1/dashboard/admin - Admin gets overall system metrics (200)")
    void testGetAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalLeads").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.totalCalls").value(greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.leadsByStatus").isMap())
                .andExpect(jsonPath("$.data.recentCalls").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/admin - Agent gets 403 Forbidden")
    void testGetAdminDashboardAgentForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/user - Agent gets assigned metrics (200)")
    void testGetUserDashboardAgent() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/user")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.myAssignedLeads").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.recentCalls").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/reports/leads - Admin gets paginated leads report (200)")
    void testGetLeadReportAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/reports/leads")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/reports/leads - Agent gets 403 Forbidden")
    void testGetLeadReportAgentForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/reports/leads")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/v1/reports/calls - Admin gets call reports (200)")
    void testGetCallReportAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/reports/calls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/reports/calls - Agent gets 403 Forbidden")
    void testGetCallReportAgentForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/reports/calls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/v1/reports/employees - Admin gets employee activity report (200)")
    void testGetEmployeeActivityReportAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/reports/employees")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/reports/employees - Agent gets 403 Forbidden")
    void testGetEmployeeActivityReportAgentForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/reports/employees")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/v1/reports/projects - Admin gets project report (200)")
    void testGetProjectReportAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/reports/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/reports/sales - Admin gets sales report (200)")
    void testGetSalesReportAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/reports/sales")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Reports and Dashboard - 401 Unauthorized when unauthenticated")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(get("/api/v1/reports/sales"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
