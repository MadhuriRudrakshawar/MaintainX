package com.tus.maintainx.controller;

import com.tus.maintainx.dto.MaintenanceWindowCreateRequestDTO;
import com.tus.maintainx.dto.MaintenanceWindowResponseDTO;
import com.tus.maintainx.dto.MaintenanceWindowUpdateRequestDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    void getAllMWTest() throws Exception {
        MaintenanceWindowResponseDTO dto1 = MaintenanceWindowResponseDTO.builder()
                .id(1L).title("MW1").windowStatus("PENDING").build();
        MaintenanceWindowResponseDTO dto2 = MaintenanceWindowResponseDTO.builder()
                .id(2L).title("MW2").windowStatus("PENDING").build();

        when(service.getAll()).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/v1/maintenance-windows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(service).getAll();
    }

    @Test
    void getByIdMWTest() throws Exception {
        MaintenanceWindowResponseDTO dto = MaintenanceWindowResponseDTO.builder()
                .id(10L).title("Patch").windowStatus("PENDING").build();

        when(service.getById(10L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/maintenance-windows/{id}", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Patch"));

        verify(service).getById(10L);
    }

    @Test
    void updateMWTest() throws Exception {
        long id = 10L;

        MaintenanceWindowResponseDTO resp = MaintenanceWindowResponseDTO.builder()
                .id(id)
                .title("Updated")
                .windowStatus("PENDING")
                .build();

        when(service.update(any(Long.class), any(MaintenanceWindowUpdateRequestDTO.class)))
                .thenReturn(resp);

        String body = """
                {
                  "title": "Updated",
                  "description": "New desc",
                  "startTime": "2026-02-18T20:00:00",
                  "endTime": "2026-02-18T22:00:00",
                  "networkElementIds": [1, 2]
                }
                """;

        mockMvc.perform(put("/api/v1/maintenance-windows/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));

        verify(service).update(any(Long.class), any(MaintenanceWindowUpdateRequestDTO.class));
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
