package com.tus.maintainx.controller;

import com.tus.maintainx.config.JwtUtils;
import com.tus.maintainx.dto.AuditLogResponseDTO;
import com.tus.maintainx.enums.AuditAction;
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

@WebMvcTest(AuditController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuditService auditService;

    @MockitoBean
    JwtUtils jwtUtils;

    @Test
    void getAllAuditLogs_returns200AndRows() throws Exception {
        AuditLogResponseDTO row = AuditLogResponseDTO.builder()
                .id(10L)
                .maintenanceWindowName("MW-10")
                .action(AuditAction.APPROVED)
                .usernameRole("ROLE_APPROVER(approver1)")
                .windowStatus("APPROVED")
                .startDuration(LocalDateTime.of(2026, 3, 1, 10, 0))
                .endDuration(LocalDateTime.of(2026, 3, 1, 11, 0))
                .build();

        when(auditService.getAllLogs()).thenReturn(List.of(row));

        mockMvc.perform(get("/api/v1/maintenance-windows/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].maintenanceWindowName").value("MW-10"))
                .andExpect(jsonPath("$[0].usernameRole").value("ROLE_APPROVER(approver1)"));

        verify(auditService).getAllLogs();
    }
}
