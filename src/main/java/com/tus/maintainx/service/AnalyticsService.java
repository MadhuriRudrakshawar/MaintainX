package com.tus.maintainx.service;

import com.tus.maintainx.dto.AnalyticsDashboardResponseDTO;
import com.tus.maintainx.dto.ApprovedWindowTimelineDTO;
import com.tus.maintainx.entity.MaintenanceWindowEntity;
import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.repository.MaintenanceWindowRepository;
import com.tus.maintainx.repository.NetworkElementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private static final String UNKNOWN = "UNKNOWN";
    private static final String UNTITLED = "UNTITLED";
    private static final String APPROVED = "APPROVED";

    private final MaintenanceWindowRepository maintenanceWindowRepository;
    private final NetworkElementRepository networkElementRepository;

    public AnalyticsDashboardResponseDTO getDashboard() {
        List<MaintenanceWindowEntity> windows = maintenanceWindowRepository.findAll();
        List<NetworkElementEntity> elements = networkElementRepository.findAll();

        Map<String, Long> maintenanceStatusCounts = new LinkedHashMap<>();
        Map<String, Long> elementsByType = new LinkedHashMap<>();
        Map<String, Long> elementsByStatus = new LinkedHashMap<>();

        for (MaintenanceWindowEntity mw : windows) {
            String status = normalize(mw.getWindowStatus(), UNKNOWN);
            increment(maintenanceStatusCounts, status);
        }

        for (NetworkElementEntity ne : elements) {
            increment(elementsByType, normalize(ne.getElementType(), UNKNOWN));
            increment(elementsByStatus, normalize(ne.getStatus(), UNKNOWN));
        }

        List<ApprovedWindowTimelineDTO> approvedWindowTimeline = windows.stream()
                .filter(mw -> APPROVED.equalsIgnoreCase(normalize(mw.getWindowStatus(), "")))
                .sorted(Comparator.comparing(MaintenanceWindowEntity::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(mw -> ApprovedWindowTimelineDTO.builder()
                        .title(normalize(mw.getTitle(), UNTITLED))
                        .startTime(mw.getStartTime())
                        .endTime(mw.getEndTime())
                        .build())
                .toList();

        return AnalyticsDashboardResponseDTO.builder()
                .maintenanceStatusCounts(sortByKey(maintenanceStatusCounts))
                .elementsByType(sortByKey(elementsByType))
                .elementsByStatus(sortByKey(elementsByStatus))
                .approvedWindowTimeline(approvedWindowTimeline)
                .build();
    }

    private Map<String, Long> sortByKey(Map<String, Long> input) {
        Map<String, Long> sorted = new LinkedHashMap<>();
        input.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> sorted.put(e.getKey(), e.getValue()));
        return sorted;
    }

    private void increment(Map<String, Long> map, String key) {
        map.put(key, map.getOrDefault(key, 0L) + 1L);
    }

    private String normalize(String value, String fallback) {
        if (value == null) return fallback;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }
}
