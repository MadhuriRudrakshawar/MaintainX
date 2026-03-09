package com.tus.maintainx.service;

import com.tus.maintainx.dto.AuditLogResponseDTO;
import com.tus.maintainx.entity.AuditLogEntity;
import com.tus.maintainx.enums.AuditAction;
import com.tus.maintainx.enums.AuditEntityType;
import com.tus.maintainx.exception.BadRequestException;
import com.tus.maintainx.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void all_ok() {
        AuditLogEntity log = AuditLogEntity.builder()
                .id(100L)
                .entityType(AuditEntityType.MAINTENANCE_WINDOW)
                .entityId(1L)
                .action(AuditAction.APPROVED)
                .username("approver1")
                .roleName("APPROVER")
                .details("Approved MW-1")
                .createdAt(LocalDateTime.of(2026, 3, 1, 12, 0))
                .build();

        when(auditLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(log));

        List<AuditLogResponseDTO> result = auditService.getAll();

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals("MAINTENANCE_WINDOW", result.get(0).getEntityType());
        assertEquals(1L, result.get(0).getEntityId());
        assertEquals("APPROVED", result.get(0).getAction());
        assertEquals("approver1", result.get(0).getUsername());
        assertEquals("APPROVER", result.get(0).getRoleName());
        assertEquals("Approved MW-1", result.get(0).getDetails());
        assertEquals(LocalDateTime.of(2026, 3, 1, 12, 0), result.get(0).getCreatedAt());
    }

    @Test
    void type_bad() {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> auditService.getByEntityType("invalid"));
        assertEquals("Invalid entityType: invalid", ex.getMessage());
    }
}
