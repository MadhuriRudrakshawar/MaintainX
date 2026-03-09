package com.tus.maintainx.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MaintenanceWindowResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String windowStatus;
    private String executionStatus;

    private String requestedByUsername;
    private String rejectionReason;

    private String decidedBy;
    private LocalDateTime decidedAt;

    private List<Long> networkElementIds;
    private List<String> networkElementCodes;
    private List<String> networkElementNames;
}
