package com.tus.maintainx.validation;

import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.exception.BadRequestException;
import com.tus.maintainx.exception.OverlapException;
import com.tus.maintainx.repository.MaintenanceWindowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MaintenanceWindowValidator {

    private final MaintenanceWindowRepository maintenanceWindowRepository;

    public void validateRequest(LocalDateTime start, LocalDateTime end, List<Long> networkElementIds) {
        validateDateRange(start, end);
        validateNetworkElements(networkElementIds);
    }

    public void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BadRequestException("Start time and end time are required");
        }

        if (!start.isBefore(end)) {
            throw new BadRequestException("End time must be after start time");
        }

        if (start.isBefore(LocalDateTime.now()) || end.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Past date/time is not allowed");
        }
    }

    public void validateNetworkElements(List<Long> networkElementIds) {
        if (networkElementIds == null || networkElementIds.isEmpty()) {
            throw new BadRequestException("Please select at least one network element");
        }
    }

    public void validateExistingNetworkElements(List<Long> networkElementIds, List<NetworkElementEntity> elements) {
        if (elements.size() != networkElementIds.size()) {
            throw new BadRequestException("Some Network Element IDs are invalid");
        }
    }

    public void checkForOverlap(List<NetworkElementEntity> elements, LocalDateTime start, LocalDateTime end) {
        for (NetworkElementEntity element : elements) {
            boolean overlap = maintenanceWindowRepository.existsOverlappingMWindow(
                    element.getId(),
                    start,
                    end
            );

            if (overlap) {
                throw new OverlapException(
                        "Overlap detected: maintenance window already exists for element '" + element.getName() + "'"
                );
            }
        }
    }
}
