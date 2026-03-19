/**
 * Service class for audit.
 * Handles business operations for audit.
 */

package com.tus.maintainx.service;

import com.tus.maintainx.dto.AuditLogResponseDTO;
import com.tus.maintainx.entity.AuditLogEntity;
import com.tus.maintainx.entity.UserEntity;
import com.tus.maintainx.enums.AuditAction;
import com.tus.maintainx.enums.AuditEntityType;
import com.tus.maintainx.exception.BadRequestException;
import com.tus.maintainx.repository.AuditLogRepository;
import com.tus.maintainx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;


    public void log(Long entityId, AuditAction action, String details) {
        log(AuditEntityType.MAINTENANCE_WINDOW, entityId, action, details);
    }

    
    public void log(AuditEntityType entityType, Long entityId, AuditAction action, String details) {
        String username = getAuthenticatedUsername();

        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new BadRequestException("Authenticated user not found in database: " + username);
        }

        String roleName = "";
        if (user.getRole() != null) {
            roleName = user.getRole();
        }

        AuditLogEntity log = AuditLogEntity.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .username(username)
                .roleName(roleName)
                .details(details)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }

    public List<AuditLogResponseDTO> getAll() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<AuditLogResponseDTO> getByEntityType(String entityType) {
        AuditEntityType type = parseEntityType(entityType);

        return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(type)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<AuditLogResponseDTO> getByEntity(String entityType, Long entityId) {
        AuditEntityType type = parseEntityType(entityType);

        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(type, entityId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private AuditLogResponseDTO toDto(AuditLogEntity e) {
        return AuditLogResponseDTO.builder()
                .id(e.getId())
                .entityType(e.getEntityType().name())
                .entityId(e.getEntityId())
                .action(e.getAction().name())
                .username(e.getUsername())
                .roleName(e.getRoleName())
                .details(e.getDetails())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private AuditEntityType parseEntityType(String value) {
        try {
            return AuditEntityType.valueOf(value.trim().toUpperCase());
        } catch (Exception ex) {
            throw new BadRequestException("Invalid entityType: " + value);
        }
    }

    private String getAuthenticatedUsername() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        String username = authentication != null ? authentication.getName() : null;

        if (username == null || username.isBlank()) {
            throw new BadRequestException("Authenticated user not found");
        }
        return username;
    }
}
