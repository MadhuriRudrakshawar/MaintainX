package com.tus.maintainx.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalyticsDashboardResponseDTO {
    private Map<String, Long> maintenanceStatusCounts;
    private Map<String, Long> elementsByType;
    private Map<String, Long> elementsByStatus;
    private List<ApprovedWindowTimelineDTO> approvedWindowTimeline;
}
