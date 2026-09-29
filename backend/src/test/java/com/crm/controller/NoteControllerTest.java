package com.crm.controller;

import com.crm.dto.request.NoteCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class NoteControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("GET /api/v1/leads/{leadId}/notes - 401 Unauthorized when unauthenticated")
    void testGetNotesUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/leads/1/notes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("GET /api/v1/leads/{leadId}/notes - Admin can retrieve notes (200)")
    void testGetNotesAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/leads/1/notes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/v1/leads/{leadId}/notes - Assigned Agent can retrieve notes (200)")
    void testGetNotesAssignedAgent() throws Exception {
        mockMvc.perform(get("/api/v1/leads/1/notes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/leads/{leadId}/notes - Non-existent lead returns 404")
    void testGetNotesNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/leads/999999/notes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/v1/leads/{leadId}/notes - Unassigned Agent 2 gets 403 Forbidden (IDOR barrier)")
    void testGetNotesAgentIdorForbidden() throws Exception {
        // Lead 2 belongs only to Agent 1. Agent 2 must be blocked with 403 Forbidden.
        mockMvc.perform(get("/api/v1/leads/2/notes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agent2Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("POST /api/v1/leads/{leadId}/notes - Admin can add note (201)")
    void testAddNoteAdmin() throws Exception {
        NoteCreateRequest req = new NoteCreateRequest("Admin internal review note");

        mockMvc.perform(post("/api/v1/leads/1/notes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("Admin internal review note"))
                .andExpect(jsonPath("$.data.userName").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/leads/{leadId}/notes - Assigned Agent can add note (201)")
    void testAddNoteAssignedAgent() throws Exception {
        NoteCreateRequest req = new NoteCreateRequest("Agent follow-up note from phone call");

        mockMvc.perform(post("/api/v1/leads/1/notes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("Agent follow-up note from phone call"));
    }

    @Test
    @DisplayName("POST /api/v1/leads/{leadId}/notes - Blank content returns 400 Validation Error")
    void testAddNoteValidationError() throws Exception {
        NoteCreateRequest req = new NoteCreateRequest("");

        mockMvc.perform(post("/api/v1/leads/1/notes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isMap());
    }

    @Test
    @DisplayName("POST /api/v1/leads/{leadId}/notes - Non-existent lead returns 404")
    void testAddNoteNotFound() throws Exception {
        NoteCreateRequest req = new NoteCreateRequest("Note on non-existent lead");

        mockMvc.perform(post("/api/v1/leads/999999/notes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/v1/leads/{leadId}/notes - Unassigned Agent 2 gets 403 Forbidden (IDOR barrier)")
    void testAddNoteAgentIdorForbidden() throws Exception {
        NoteCreateRequest req = new NoteCreateRequest("Unauthorized agent attempt to attach note");

        mockMvc.perform(post("/api/v1/leads/2/notes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agent2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
