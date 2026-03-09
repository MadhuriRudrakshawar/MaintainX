package com.tus.maintainx.controller;

import com.tus.maintainx.dto.AuditLogResponseDTO;
import com.tus.maintainx.service.AuditService;
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
public class AuditLogController {

    private final AuditService auditService;

    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    @GetMapping
    public ResponseEntity<List<AuditLogResponseDTO>> getAll() {
        return ResponseEntity.ok(auditService.getAll());
    }

    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    @GetMapping("/type/{entityType}")
    public ResponseEntity<List<AuditLogResponseDTO>> getByEntityType(@PathVariable String entityType) {
        return ResponseEntity.ok(auditService.getByEntityType(entityType));
    }

    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    @GetMapping("/type/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLogResponseDTO>> getByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(auditService.getByEntity(entityType, entityId));
    }
}