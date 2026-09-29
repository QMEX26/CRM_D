package com.crm.controller;

import com.crm.dto.request.UserCreateRequest;
import com.crm.dto.request.UserStatusRequest;
import com.crm.dto.request.UserUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("GET /api/v1/users should succeed for ADMIN and forbid USER")
    void testSearchUsersRbac() throws Exception {
        // Admin access succeeds
        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", not(empty())));

        // Agent access forbidden (403)
        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isForbidden());

        // Unauthenticated (401)
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/users/active should be accessible to both ADMIN and USER")
    void testGetActiveUsers() throws Exception {
        // Admin access
        mockMvc.perform(get("/api/v1/users/active")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", not(empty())));

        // Agent access
        mockMvc.perform(get("/api/v1/users/active")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", not(empty())));
    }

    @Test
    @DisplayName("POST /api/v1/users should validate creation, duplicate emails, and RBAC")
    void testCreateUserFlow() throws Exception {
        // Validation failure (blank name and email)
        UserCreateRequest invalid = new UserCreateRequest("", "", "", "password123", "USER", "ACTIVE", "SHIFT_1000_1900");
        mockMvc.perform(post("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        // Duplicate email (409 Conflict)
        UserCreateRequest duplicate = new UserCreateRequest("Duplicate Admin", "admin@crm.com", "+91 99999 88888", "pass123", "ADMIN", "ACTIVE", "SHIFT_1000_1900");
        mockMvc.perform(post("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());

        // Agent attempt forbidden (403)
        UserCreateRequest validReq = new UserCreateRequest("New Agent", "newagent@crm.com", "+91 99999 77777", "pass1234", "USER", "ACTIVE", "SHIFT_1000_1900");
        mockMvc.perform(post("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isForbidden());

        // Admin creation succeeds (201 Created)
        mockMvc.perform(post("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email", is("newagent@crm.com")))
                .andExpect(jsonPath("$.data.name", is("New Agent")));
    }

    @Test
    @DisplayName("GET, PUT, PATCH on /api/v1/users/{id} should handle valid, 404 and RBAC")
    void testUserCrudAndStatus() throws Exception {
        // Non-existent ID returns 404
        mockMvc.perform(get("/api/v1/users/999999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        // Agent cannot get user by id
        mockMvc.perform(get("/api/v1/users/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isForbidden());

        // Admin update user
        UserUpdateRequest updateReq = new UserUpdateRequest("Updated Admin Name", "+91 98765 00001", "ADMIN", "ACTIVE", "SHIFT_1000_1900");
        mockMvc.perform(put("/api/v1/users/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("Updated Admin Name")));

        // Admin toggle status
        UserStatusRequest statusReq = new UserStatusRequest("ACTIVE");
        mockMvc.perform(patch("/api/v1/users/1/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk());
    }
}
