package com.tus.maintainx.controller;


import com.tus.maintainx.dto.AuditLogResponseDTO;
import com.tus.maintainx.service.AuditService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance-windows")
@AllArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogResponseDTO>> getAllAuditLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }
}
