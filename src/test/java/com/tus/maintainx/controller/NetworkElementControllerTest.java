package com.tus.maintainx.controller;

import com.tus.maintainx.dto.NetworkElementCreateDTO;
import com.tus.maintainx.dto.NetworkElementResponseDTO;
import com.tus.maintainx.service.NetworkElementService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NetworkElementController.class)
class NetworkElementControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    NetworkElementService service;

    @Test
    void createCheckTest() throws Exception {
        NetworkElementCreateDTO dto = new NetworkElementCreateDTO(
                "NE-001", "Core Router", "ROUTER", "Dublin", "ACTIVE"
        );

        NetworkElementResponseDTO saved = new NetworkElementResponseDTO(
                10L, "NE-001", "Core Router", "ROUTER", "Dublin", "ACTIVE"
        );

        Mockito.when(service.create(any(NetworkElementCreateDTO.class))).thenReturn(saved);

        mvc.perform(post("/api/v1/network-elements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.elementCode").value("NE-001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }


    @Test
    void deactivateTest() throws Exception {
        NetworkElementResponseDTO updated = new NetworkElementResponseDTO(
                1L, "NE-001", "Core Router", "ROUTER", "Dublin", "DEACTIVE"
        );
        Mockito.when(service.deactivate(1L)).thenReturn(updated);

        mvc.perform(patch("/api/v1/network-elements/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEACTIVE"));
    }
}
