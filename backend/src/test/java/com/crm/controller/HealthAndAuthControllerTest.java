package com.crm.controller;

import com.crm.dto.request.ChangePasswordRequest;
import com.crm.dto.request.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HealthAndAuthControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("Health check should return UP and 200")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", containsString("CallingCRM")));
    }

    @Test
    @DisplayName("Login with valid credentials should return JWT and user details")
    void testValidLogin() throws Exception {
        LoginRequest request = new LoginRequest("admin@crm.com", "admin123");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.email", is("admin@crm.com")))
                .andExpect(jsonPath("$.data.role", is("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Login with wrong password should return 401 Unauthorized")
    void testLoginWithWrongPassword() throws Exception {
        LoginRequest request = new LoginRequest("admin@crm.com", "wrongpassword");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("Login with unknown email should return 401 Unauthorized")
    void testLoginWithUnknownEmail() throws Exception {
        LoginRequest request = new LoginRequest("unknown@crm.com", "admin123");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("Login with empty fields should return 400 Bad Request")
    void testLoginWithEmptyFields() throws Exception {
        LoginRequest request = new LoginRequest("", "");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.details", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me should return current user info when authenticated")
    void testGetCurrentUser() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email", is("admin@crm.com")));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email", is("agent@crm.com")));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me without token should return 401")
    void testGetCurrentUserUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/auth/me with invalid token should return 401")
    void testGetCurrentUserInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token-xyz"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Change password should validate old password and update")
    void testChangePasswordFlow() throws Exception {
        // Wrong current password
        ChangePasswordRequest badOld = new ChangePasswordRequest("wrongold", "newpass123");
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agent2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badOld)))
                .andExpect(status().isBadRequest());

        // Short new password (<6)
        ChangePasswordRequest tooShort = new ChangePasswordRequest("agent123", "123");
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agent2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tooShort)))
                .andExpect(status().isBadRequest());

        // Valid change
        ChangePasswordRequest validChange = new ChangePasswordRequest("agent123", "updated123");
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agent2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validChange)))
                .andExpect(status().isOk());

        // Verify login works with new password
        obtainToken("agent2@crm.com", "updated123");

        // Revert password back for idempotency
        ChangePasswordRequest revert = new ChangePasswordRequest("updated123", "agent123");
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agent2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(revert)))
                .andExpect(status().isOk());
    }
}
