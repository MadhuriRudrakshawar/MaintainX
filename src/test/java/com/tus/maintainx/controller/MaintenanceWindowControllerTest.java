package com.tus.maintainx.controller;

import com.tus.maintainx.dto.MaintenanceWindowCreateRequestDTO;
import com.tus.maintainx.dto.MaintenanceWindowResponseDTO;
import com.tus.maintainx.service.MaintenanceWindowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaintenanceWindowController.class)
class MaintenanceWindowControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MaintenanceWindowService service;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createMaintenanceWindowTest() throws Exception {

        MaintenanceWindowCreateRequestDTO req = new MaintenanceWindowCreateRequestDTO();
        req.setTitle("Core router patching");
        req.setDescription("Planned");
        req.setStartTime(LocalDateTime.of(2026, 2, 18, 20, 0, 0));
        req.setEndTime(LocalDateTime.of(2026, 2, 18, 22, 0, 0));
        req.setNetworkElementIds(List.of(1L, 2L));
        req.setRequestedById(10L);

        MaintenanceWindowResponseDTO resp = MaintenanceWindowResponseDTO.builder()
                .id(99L)
                .title(req.getTitle())
                .description(req.getDescription())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .windowStatus("PENDING")
                .requestedByUsername("engineer1")
                .networkElementIds(List.of(1L, 2L))
                .networkElementNames(List.of("Core Router A", "Edge Switch B"))
                .build();

        when(service.create(any(MaintenanceWindowCreateRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/maintenance-windows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Core router patching"));


        verify(service).create(any(MaintenanceWindowCreateRequestDTO.class));
    }


    @Test
    void deleteMWTest() throws Exception {
        long id = 10L;

        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/v1/maintenance-windows/{id}", id))
                .andExpect(status().isNoContent());

        verify(service).delete(id);
    }

}
