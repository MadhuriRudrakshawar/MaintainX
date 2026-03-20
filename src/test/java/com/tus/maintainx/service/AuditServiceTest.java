package com.tus.maintainx.service;

import com.tus.maintainx.dto.AuditLogResponseDTO;
import com.tus.maintainx.entity.AuditLogEntity;
import com.tus.maintainx.entity.UserEntity;
import com.tus.maintainx.enums.AuditAction;
import com.tus.maintainx.enums.AuditEntityType;
import com.tus.maintainx.exception.BadRequestException;
import com.tus.maintainx.repository.AuditLogRepository;
import com.tus.maintainx.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuditService auditService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(String username) {
        SecurityContext ctx = org.mockito.Mockito.mock(SecurityContext.class);
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

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

    @Test
    void getByEntityTypeTest() {
        AuditLogEntity log = AuditLogEntity.builder()
                .id(101L)
                .entityType(AuditEntityType.MAINTENANCE_WINDOW)
                .entityId(8L)
                .action(AuditAction.UPDATED)
                .username("engineer1")
                .roleName("ENGINEER")
                .details("Updated MW-8")
                .createdAt(LocalDateTime.of(2026, 3, 2, 10, 15))
                .build();

        when(auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(AuditEntityType.MAINTENANCE_WINDOW))
                .thenReturn(List.of(log));

        List<AuditLogResponseDTO> result = auditService.getByEntityType("  maintenance_window ");

        assertEquals(1, result.size());
        assertEquals("UPDATED", result.get(0).getAction());
        assertEquals("MAINTENANCE_WINDOW", result.get(0).getEntityType());
        verify(auditLogRepository).findByEntityTypeOrderByCreatedAtDesc(AuditEntityType.MAINTENANCE_WINDOW);
    }

    @Test
    void getByEntityTest() {
        AuditLogEntity log = AuditLogEntity.builder()
                .id(102L)
                .entityType(AuditEntityType.MAINTENANCE_WINDOW)
                .entityId(9L)
                .action(AuditAction.REJECTED)
                .username("approver1")
                .roleName("APPROVER")
                .details("Rejected MW-9")
                .createdAt(LocalDateTime.of(2026, 3, 3, 11, 45))
                .build();

        when(auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(AuditEntityType.MAINTENANCE_WINDOW, 9L))
                .thenReturn(List.of(log));

        List<AuditLogResponseDTO> result = auditService.getByEntity(" maintenance_window ", 9L);

        assertEquals(1, result.size());
        assertEquals(9L, result.get(0).getEntityId());
        assertEquals("Rejected MW-9", result.get(0).getDetails());
        verify(auditLogRepository).findByEntityTypeAndEntityIdOrderByCreatedAtDesc(AuditEntityType.MAINTENANCE_WINDOW, 9L);
    }

    @Test
    void logEntityTypeTest() {
        mockAuthenticatedUser("approver1");

        UserEntity user = new UserEntity();
        user.setUsername("approver1");
        user.setRole("APPROVER");
        when(userRepository.findByUsername("approver1")).thenReturn(user);

        auditService.log(AuditEntityType.MAINTENANCE_WINDOW, 5L, AuditAction.APPROVED, "approved");

        verify(auditLogRepository).save(any(AuditLogEntity.class));
    }

    @Test
    void logEmptyRoleTest() {
        mockAuthenticatedUser("engineer1");

        UserEntity user = new UserEntity();
        user.setUsername("engineer1");
        user.setRole(null);
        when(userRepository.findByUsername("engineer1")).thenReturn(user);

        auditService.log(7L, AuditAction.CREATED, "created");

        verify(auditLogRepository).save(any(AuditLogEntity.class));
    }


    @Test
    void logAuthenticationMissingTest() {
        SecurityContext ctx = org.mockito.Mockito.mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(ctx);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> auditService.log(1L, AuditAction.CREATED, "created"));

        assertEquals("Authenticated user not found", ex.getMessage());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void getByEntityInvalidEntityTest() {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> auditService.getByEntity("bad", 1L));
        assertTrue(ex.getMessage().contains("Invalid entityType: bad"));
    }
}
