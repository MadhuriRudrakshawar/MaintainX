package com.tus.maintainx.service;

import com.tus.maintainx.dto.AuditLogResponseDTO;
import com.tus.maintainx.entity.AuditLog;
import com.tus.maintainx.entity.MaintenanceWindowEntity;
import com.tus.maintainx.enums.AuditAction;
import com.tus.maintainx.exception.NotFoundException;
import com.tus.maintainx.repository.AuditLogRepository;
import com.tus.maintainx.repository.MaintenanceWindowRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final MaintenanceWindowRepository maintenanceWindowRepository;

    public AuditService(AuditLogRepository auditLogRepository,
                        MaintenanceWindowRepository maintenanceWindowRepository) {
        this.auditLogRepository = auditLogRepository;
        this.maintenanceWindowRepository = maintenanceWindowRepository;
    }

    public void log(Long maintenanceWindowId, AuditAction action, String details) {

        MaintenanceWindowEntity mw = maintenanceWindowRepository.findById(maintenanceWindowId)
                .orElseThrow(() -> new NotFoundException("Maintenance window not found: " + maintenanceWindowId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.getName() != null) ? auth.getName() : "SYSTEM";

        String role = "UNKNOWN";
        if (auth != null) {
            role = auth.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("UNKNOWN");
        }

        AuditLog log = new AuditLog();
        log.setMaintenanceWindow(mw);
        log.setAction(action);
        log.setActorUsername(username);
        log.setActorRole(role);
        log.setDetails(details);

        auditLogRepository.save(log);
    }

    public List<AuditLogResponseDTO> getLogs(Long maintenanceWindowId) {
        return auditLogRepository
                .findByMaintenanceWindow_IdOrderByCreatedAtAsc(maintenanceWindowId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<AuditLogResponseDTO> getAllLogs() {
        List<AuditLogResponseDTO> logs = auditLogRepository
                .findAllByOrderByCreatedAtAsc()
                .stream()
                .map(this::toResponseDto)
                .toList();

        if (!logs.isEmpty()) {
            return logs;
        }

        return maintenanceWindowRepository.findAll()
                .stream()
                .map(w -> new AuditLogResponseDTO(
                        w.getId(),
                        w.getTitle(),
                        AuditAction.SUBMITTED,
                        w.getRequestedBy() != null
                                ? w.getRequestedBy().getRole() + "(" + w.getRequestedBy().getUsername() + ")"
                                : "UNKNOWN(UNKNOWN)",
                        w.getWindowStatus(),
                        w.getStartTime(),
                        w.getEndTime()
                ))
                .toList();
    }

    private AuditLogResponseDTO toResponseDto(AuditLog x) {
        return new AuditLogResponseDTO(
                x.getId(),
                x.getMaintenanceWindow().getTitle(),
                x.getAction(),
                x.getActorRole() + "(" + x.getActorUsername() + ")",
                x.getMaintenanceWindow().getWindowStatus(),
                x.getMaintenanceWindow().getStartTime(),
                x.getMaintenanceWindow().getEndTime()
        );
    }
}
