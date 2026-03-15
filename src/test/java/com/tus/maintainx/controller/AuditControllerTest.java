package com.tus.maintainx.controller;

import com.tus.maintainx.config.JwtUtils;
import com.tus.maintainx.dto.AuditLogResponseDTO;
import com.tus.maintainx.repository.UserRepository;
import com.tus.maintainx.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuditService auditService;

    @MockitoBean
    JwtUtils jwtUtils;

    @MockitoBean
    UserRepository userRepository;

    @Test
    void all_ok() throws Exception {
        AuditLogResponseDTO row = AuditLogResponseDTO.builder()
                .id(10L)
                .entityType("MAINTENANCE_WINDOW")
                .entityId(99L)
                .action("APPROVED")
                .username("approver1")
                .roleName("APPROVER")
                .details("Approved maintenance window")
                .createdAt(LocalDateTime.of(2026, 3, 1, 10, 0))
                .build();

        when(auditService.getAll()).thenReturn(List.of(row));

        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].entityType").value("MAINTENANCE_WINDOW"))
                .andExpect(jsonPath("$[0].entityId").value(99))
                .andExpect(jsonPath("$[0].action").value("APPROVED"));

        verify(auditService).getAll();
    }
}
