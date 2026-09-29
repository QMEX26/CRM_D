package com.crm.controller;

import com.crm.dto.request.ProjectCreateRequest;
import com.crm.dto.request.ProjectUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProjectControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("GET /api/v1/projects - 401 Unauthorized when unauthenticated")
    void testGetProjectsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/projects - Admin gets paginated projects list (200)")
    void testGetProjectsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("GET /api/v1/projects - Agent gets paginated projects list (200)")
    void testGetProjectsAgent() throws Exception {
        mockMvc.perform(get("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/projects - Search and status filters work (200)")
    void testSearchProjectsWithFilter() throws Exception {
        mockMvc.perform(get("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .param("search", "Cyber")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/projects/active - Both Admin and Agent can get active projects (200)")
    void testGetActiveProjects() throws Exception {
        mockMvc.perform(get("/api/v1/projects/active")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id} - Valid ID returns project (200)")
    void testGetProjectByIdSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/projects/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id} - Non-existent ID returns 404")
    void testGetProjectByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/projects/999999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/v1/projects - Admin creates project (201)")
    void testCreateProjectAdmin() throws Exception {
        ProjectCreateRequest req = new ProjectCreateRequest("Green Horizon Villas", "Luxury villas in North Bangalore", "ACTIVE");

        mockMvc.perform(post("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Green Horizon Villas"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/v1/projects - Agent cannot create project (403 Forbidden)")
    void testCreateProjectAgentForbidden() throws Exception {
        ProjectCreateRequest req = new ProjectCreateRequest("Agent Project", "Test", "ACTIVE");

        mockMvc.perform(post("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("POST /api/v1/projects - Blank project name returns 400 Validation Error")
    void testCreateProjectValidationError() throws Exception {
        ProjectCreateRequest req = new ProjectCreateRequest("", "Description", "ACTIVE");

        mockMvc.perform(post("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id} - Admin updates project (200)")
    void testUpdateProjectAdmin() throws Exception {
        ProjectUpdateRequest req = new ProjectUpdateRequest("Cyber Park Phase 1 Updated", "Updated desc", "ACTIVE");

        mockMvc.perform(put("/api/v1/projects/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Cyber Park Phase 1 Updated"));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id} - Agent cannot update project (403 Forbidden)")
    void testUpdateProjectAgentForbidden() throws Exception {
        ProjectUpdateRequest req = new ProjectUpdateRequest("Hacked Name", "Desc", "ACTIVE");

        mockMvc.perform(put("/api/v1/projects/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id} - Update non-existent project returns 404")
    void testUpdateProjectNotFound() throws Exception {
        ProjectUpdateRequest req = new ProjectUpdateRequest("Valid Name", "Valid Desc", "ACTIVE");

        mockMvc.perform(put("/api/v1/projects/999999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PATCH /api/v1/projects/{id}/status - Admin toggles project status (200)")
    void testToggleProjectStatusAdmin() throws Exception {
        mockMvc.perform(patch("/api/v1/projects/1/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("PATCH /api/v1/projects/{id}/status - Agent cannot toggle project status (403 Forbidden)")
    void testToggleProjectStatusAgentForbidden() throws Exception {
        mockMvc.perform(patch("/api/v1/projects/1/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .param("status", "COMPLETED"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
