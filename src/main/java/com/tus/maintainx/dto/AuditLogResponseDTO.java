package com.tus.maintainx.dto;


import com.tus.maintainx.enums.AuditAction;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponseDTO {

    private Long id;
    private String maintenanceWindowName;
    private AuditAction action;
    private String usernameRole;
    private String windowStatus;
    private LocalDateTime startDuration;
    private LocalDateTime endDuration;
}
