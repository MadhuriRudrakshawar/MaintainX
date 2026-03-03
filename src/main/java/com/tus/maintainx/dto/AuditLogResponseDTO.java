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
    private AuditAction action;
    private String actorUsername;
    private String actorRole;
    private String details;
    private LocalDateTime createdAt;
}