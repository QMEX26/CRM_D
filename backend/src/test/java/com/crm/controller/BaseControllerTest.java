package com.crm.controller;

import com.crm.dto.request.LoginRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String adminToken;
    protected String agentToken;
    protected String agent2Token;

    @BeforeEach
    void setUpTokens() throws Exception {
        if (adminToken == null) {
            adminToken = obtainToken("admin@crm.com", "admin123");
        }
        if (agentToken == null) {
            agentToken = obtainToken("agent@crm.com", "agent123");
        }
        if (agent2Token == null) {
            agent2Token = obtainToken("agent2@crm.com", "agent123");
        }
    }

    protected String obtainToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseBody);
        return rootNode.path("data").path("token").asText();
    }
}
