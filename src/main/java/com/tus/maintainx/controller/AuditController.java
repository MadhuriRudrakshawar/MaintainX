package com.tus.maintainx.controller;


import com.tus.maintainx.dto.AuditLogResponseDTO;
import com.tus.maintainx.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance-windows")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogResponseDTO>> getAllAuditLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }

    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<List<AuditLogResponseDTO>> getAuditLogs(@PathVariable("id") Long maintenanceWindowId) {
        return ResponseEntity.ok(auditService.getLogs(maintenanceWindowId));
    }
}
