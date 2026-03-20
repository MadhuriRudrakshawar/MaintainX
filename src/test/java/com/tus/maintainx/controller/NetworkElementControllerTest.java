package com.tus.maintainx.controller;

import com.tus.maintainx.config.JwtUtils;
import com.tus.maintainx.dto.NetworkElementCreateDTO;
import com.tus.maintainx.dto.NetworkElementResponseDTO;
import com.tus.maintainx.repository.UserRepository;
import com.tus.maintainx.security.JwtAuthenticationFilter;
import com.tus.maintainx.service.NetworkElementService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NetworkElementController.class)
@AutoConfigureMockMvc(addFilters = false)
class NetworkElementControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    NetworkElementService service;

    @MockitoBean
    JwtUtils jwtUtils;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createCheckTest() throws Exception {
        NetworkElementCreateDTO dto = new NetworkElementCreateDTO(
                "Core Router", "ROUTER", "Dublin", "ACTIVE"
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
    void getAllTest() throws Exception {
        List<NetworkElementResponseDTO> list = List.of(
                new NetworkElementResponseDTO(1L, "NE-001", "Core Router", "ROUTER", "Dublin", "ACTIVE"),
                new NetworkElementResponseDTO(2L, "NE-002", "Edge Switch", "SWITCH", "Cork", "DEACTIVE")
        );

        Mockito.when(service.getAll()).thenReturn(list);

        mvc.perform(get("/api/v1/network-elements"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].elementCode").value("NE-001"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].status").value("DEACTIVE"));
    }

    @Test
    void getByIdTest() throws Exception {
        NetworkElementResponseDTO dto = new NetworkElementResponseDTO(
                7L, "NE-007", "Radio Unit", "RAN", "Galway", "ACTIVE"
        );

        Mockito.when(service.getByElementId(7L)).thenReturn(dto);

        mvc.perform(get("/api/v1/network-elements/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.elementType").value("RAN"))
                .andExpect(jsonPath("$.region").value("Galway"));
    }

    @Test
    void updateWithPutTest() throws Exception {
        NetworkElementCreateDTO req = new NetworkElementCreateDTO(
                "Core Router Y", "ROUTER", "Dublin", "ACTIVE"
        );

        NetworkElementResponseDTO updated = new NetworkElementResponseDTO(
                1L, "NE-001", "Core Router Y", "ROUTER", "Dublin", "ACTIVE"
        );

        Mockito.when(service.update(Mockito.eq(1L), any(NetworkElementCreateDTO.class))).thenReturn(updated);

        mvc.perform(put("/api/v1/network-elements/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Core Router Y"));
    }

    @Test
    void updateWithPatchNotAllowedTest() throws Exception {
        NetworkElementCreateDTO req = new NetworkElementCreateDTO(
                "Core Router Z", "ROUTER", "Dublin", "ACTIVE"
        );

        mvc.perform(patch("/api/v1/network-elements/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isMethodNotAllowed());
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

    @Test
    void activateTest() throws Exception {
        NetworkElementResponseDTO updated = new NetworkElementResponseDTO(
                1L, "NE-001", "Core Router", "ROUTER", "Dublin", "ACTIVE"
        );
        Mockito.when(service.activate(1L)).thenReturn(updated);

        mvc.perform(patch("/api/v1/network-elements/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void deleteTest() throws Exception {
        mvc.perform(delete("/api/v1/network-elements/99"))
                .andExpect(status().isNoContent());

        verify(service).delete(99L);
    }
}
