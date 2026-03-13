package com.tus.maintainx.service;

import com.tus.maintainx.dto.AnalyticsDashboardResponseDTO;
import com.tus.maintainx.dto.ApprovedWindowTimelineDTO;
import com.tus.maintainx.entity.MaintenanceWindowEntity;
import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.repository.MaintenanceWindowRepository;
import com.tus.maintainx.repository.NetworkElementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        Map<String, Long> windowsByDate = new LinkedHashMap<>();
        Map<String, Long> bookedSlotsByHour = initHourlySlots();
        Map<String, Long> bookedSlotsByDateHour = new LinkedHashMap<>();
        Map<String, Long> elementsByType = new LinkedHashMap<>();
        Map<String, Long> elementsByStatus = new LinkedHashMap<>();
        Map<String, Long> impactedElementCounts = new LinkedHashMap<>();

        for (MaintenanceWindowEntity mw : windows) {
            String status = normalize(mw.getWindowStatus(), UNKNOWN);
            increment(maintenanceStatusCounts, status);

            String startDate = mw.getStartTime() == null ? UNKNOWN : mw.getStartTime().toLocalDate().toString();
            increment(windowsByDate, startDate);
            incrementBookedHourSlots(bookedSlotsByHour, mw.getStartTime(), mw.getEndTime());
            incrementBookedDateHourSlots(bookedSlotsByDateHour, mw.getStartTime(), mw.getEndTime());

            for (NetworkElementEntity ne : mw.getNetworkElements()) {
                increment(impactedElementCounts, normalize(ne.getName(), UNKNOWN));
            }
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
        Map<String, Long> topImpactedElements = impactedElementCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .collect(
                        LinkedHashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue()),
                        LinkedHashMap::putAll
                );

        return AnalyticsDashboardResponseDTO.builder()
                .maintenanceStatusCounts(sortByKey(maintenanceStatusCounts))
                .windowsByDate(sortByKey(windowsByDate))
                .bookedSlotsByHour(bookedSlotsByHour)
                .bookedSlotsByDateHour(sortByKey(bookedSlotsByDateHour))
                .elementsByType(sortByKey(elementsByType))
                .elementsByStatus(sortByKey(elementsByStatus))
                .approvedWindowTimeline(approvedWindowTimeline)
                .topImpactedElements(topImpactedElements)
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

    private Map<String, Long> initHourlySlots() {
        Map<String, Long> slots = new LinkedHashMap<>();
        for (int hour = 0; hour < 24; hour++) {
            String label = String.format("%02d:00-%02d:00", hour, (hour + 1) % 24);
            slots.put(label, 0L);
        }
        return slots;
    }

    private void incrementBookedHourSlots(Map<String, Long> slots, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !start.isBefore(end)) {
            return;
        }

        LocalDateTime cursor = start;
        while (cursor.isBefore(end)) {
            int hour = cursor.getHour();
            String label = String.format("%02d:00-%02d:00", hour, (hour + 1) % 24);
            slots.put(label, slots.getOrDefault(label, 0L) + 1L);
            cursor = cursor.plusHours(1).withMinute(0).withSecond(0).withNano(0);
            if (cursor.isBefore(start.plusHours(1))) {
                cursor = start.plusHours(1);
            }
        }
    }

    private void incrementBookedDateHourSlots(Map<String, Long> slots, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !start.isBefore(end)) {
            return;
        }

        LocalDateTime cursor = start;
        while (cursor.isBefore(end)) {
            String key = String.format("%s|%02d", cursor.toLocalDate(), cursor.getHour());
            slots.put(key, slots.getOrDefault(key, 0L) + 1L);
            cursor = cursor.plusHours(1).withMinute(0).withSecond(0).withNano(0);
            if (cursor.isBefore(start.plusHours(1))) {
                cursor = start.plusHours(1);
            }
        }
    }
}
