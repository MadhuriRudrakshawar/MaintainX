package com.tus.maintainx.integration;

import com.tus.maintainx.dto.NetworkElementCreateDTO;
import com.tus.maintainx.repository.NetworkElementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class NetworkElementApiIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    NetworkElementRepository repo;

    @BeforeEach
    void cleanDb() {
        repo.deleteAll();
    }

    @Test
    @WithMockUser
    void networkElementsE2ETest() throws Exception {

        NetworkElementCreateDTO dto = new NetworkElementCreateDTO(
                "NE-100", "Edge Switch", "SWITCH", "Cork", "ACTIVE"
        );

        String createResp = mvc.perform(post("/api/v1/network-elements")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.elementCode").value("NE-100"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long id = objectMapper.readTree(createResp).get("id").asLong();

        mvc.perform(get("/api/v1/network-elements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(patch("/api/v1/network-elements/" + id + "/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEACTIVE"));

        mvc.perform(patch("/api/v1/network-elements/" + id + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mvc.perform(delete("/api/v1/network-elements/" + id))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/network-elements/" + id))
                .andExpect(status().isNotFound());
    }
}
