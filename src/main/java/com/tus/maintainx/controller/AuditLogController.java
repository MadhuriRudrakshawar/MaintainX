package com.tus.maintainx.controller;

import com.tus.maintainx.dto.AuditLogResponseDTO;
import com.tus.maintainx.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "APIs for viewing audit log records")
public class AuditLogController {

    private final AuditService auditService;

    @Operation(summary = "Get all audit logs", description = "Returns all audit log records")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    @GetMapping
    public ResponseEntity<List<AuditLogResponseDTO>> getAll() {
        return ResponseEntity.ok(auditService.getAll());
    }

    @Operation(summary = "Get audit logs by entity type", description = "Returns audit logs filtered by entity type")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    @GetMapping("/type/{entityType}")
    public ResponseEntity<List<AuditLogResponseDTO>> getByEntityType(
            @Parameter(description = "Entity type such as MAINTENANCE_WINDOW or NETWORK_ELEMENT")
            @PathVariable String entityType) {
        return ResponseEntity.ok(auditService.getByEntityType(entityType));
    }

    @Operation(summary = "Get audit logs by entity", description = "Returns audit logs for a specific entity type and entity id")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    @GetMapping("/type/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLogResponseDTO>> getByEntity(
            @Parameter(description = "Entity type such as MAINTENANCE_WINDOW or NETWORK_ELEMENT")
            @PathVariable String entityType,
            @Parameter(description = "Entity id")
            @PathVariable Long entityId) {
        return ResponseEntity.ok(auditService.getByEntity(entityType, entityId));
    }
}