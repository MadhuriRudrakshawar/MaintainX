package com.tus.maintainx.service;

import com.tus.maintainx.dto.AuditLogResponseDTO;
import com.tus.maintainx.entity.AuditLog;
import com.tus.maintainx.entity.MaintenanceWindowEntity;
import com.tus.maintainx.entity.UserEntity;
import com.tus.maintainx.enums.AuditAction;
import com.tus.maintainx.repository.AuditLogRepository;
import com.tus.maintainx.repository.MaintenanceWindowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private MaintenanceWindowRepository maintenanceWindowRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void getAllLogs_withAuditRows_returnsMappedAuditRows() {
        MaintenanceWindowEntity mw = new MaintenanceWindowEntity();
        mw.setId(1L);
        mw.setTitle("MW-1");
        mw.setWindowStatus("APPROVED");
        mw.setStartTime(LocalDateTime.of(2026, 3, 1, 10, 0));
        mw.setEndTime(LocalDateTime.of(2026, 3, 1, 12, 0));

        AuditLog log = new AuditLog();
        log.setId(100L);
        log.setMaintenanceWindow(mw);
        log.setAction(AuditAction.APPROVED);
        log.setActorRole("ROLE_APPROVER");
        log.setActorUsername("approver1");

        when(auditLogRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(log));

        List<AuditLogResponseDTO> result = auditService.getAllLogs();

        assertEquals(1, result.size());
        assertEquals("MW-1", result.get(0).getMaintenanceWindowName());
        assertEquals("ROLE_APPROVER(approver1)", result.get(0).getUsernameRole());
        verify(maintenanceWindowRepository, never()).findAll();
    }

    @Test
    void getAllLogs_withoutAuditRows_returnsFallbackFromMaintenanceWindows() {
        when(auditLogRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of());

        UserEntity user = new UserEntity();
        user.setUsername("engineer1");
        user.setRole("ENGINEER");

        MaintenanceWindowEntity mw = new MaintenanceWindowEntity();
        mw.setId(2L);
        mw.setTitle("Fallback-MW");
        mw.setRequestedBy(user);
        mw.setWindowStatus("PENDING");
        mw.setStartTime(LocalDateTime.of(2026, 3, 2, 1, 0));
        mw.setEndTime(LocalDateTime.of(2026, 3, 2, 2, 0));

        when(maintenanceWindowRepository.findAll()).thenReturn(List.of(mw));

        List<AuditLogResponseDTO> result = auditService.getAllLogs();

        assertEquals(1, result.size());
        assertEquals(AuditAction.SUBMITTED, result.get(0).getAction());
        assertEquals("ENGINEER(engineer1)", result.get(0).getUsernameRole());
        assertEquals("PENDING", result.get(0).getWindowStatus());
    }
}
