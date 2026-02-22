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

    private String requestedByUsername;

    private List<Long> networkElementIds;
    private List<String> networkElementNames;
}
