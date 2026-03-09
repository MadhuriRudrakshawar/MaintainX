package com.tus.maintainx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponseDTO {
    private Long id;
    private String entityType;
    private Long entityId;
    private String action;
    private String username;
    private String roleName;
    private String details;
    private LocalDateTime createdAt;
}