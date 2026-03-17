package com.tus.maintainx.validation;

import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.exception.BadRequestException;
import com.tus.maintainx.exception.OverlapException;
import com.tus.maintainx.repository.MaintenanceWindowRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MaintenanceWindowValidatorTest {

    private final MaintenanceWindowRepository repository = mock(MaintenanceWindowRepository.class);
    private final MaintenanceWindowValidator validator = new MaintenanceWindowValidator(repository);

    @Test
    void validateRequestTest() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(11).withMinute(0);
        LocalDateTime end = start.plusHours(1);

        assertDoesNotThrow(() -> validator.validateRequest(start, end, List.of(1L, 2L)));
    }

    @Test
    void validateDateRange_rejectsNullStartOrEnd() {
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> validator.validateDateRange(null, end));

        assertEquals("Start time and end time are required", exception.getMessage());
    }

    @Test
    void validateDateRange_rejectsEndBeforeOrEqualStart() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> validator.validateDateRange(start, start));

        assertEquals("End time must be after start time", exception.getMessage());
    }

    @Test
    void validateDateRangePastDatesTest() {
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> validator.validateDateRange(start, end));

        assertEquals("Past date/time is not allowed", exception.getMessage());
    }

    @Test
    void validateNetworkElementsMissingIdTest() {
        List<Long> networkElementIds = List.of();

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> validator.validateNetworkElements(networkElementIds));

        assertEquals("Please select at least one network element", exception.getMessage());
    }

    @Test
    void validateRestrictedTimeRejectsOverlapTest() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(8).withMinute(30);
        LocalDateTime end = start.withHour(9).withMinute(30);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> validator.validateRestrictedTime(start, end));

        assertEquals("Request violates policy: restricted time window.", exception.getMessage());
    }

    @Test
    void validateRestrictedTimeTest() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(11).withMinute(0);
        LocalDateTime end = start.plusHours(1);

        assertDoesNotThrow(() -> validator.validateRestrictedTime(start, end));
    }

    @Test
    void validateExistingNetworkElementsTest() {
        List<Long> networkElementIds = List.of(1L, 2L);
        List<NetworkElementEntity> networkElements = List.of(new NetworkElementEntity());

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> validator.validateExistingNetworkElements(networkElementIds, networkElements));

        assertEquals("Some Network Element IDs are invalid", exception.getMessage());
    }

    @Test
    void checkForOverlap_rejectsTest() {
        NetworkElementEntity element = new NetworkElementEntity();
        element.setId(5L);
        element.setName("Router-A");
        List<NetworkElementEntity> elements = List.of(element);

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(11).withMinute(0);
        LocalDateTime end = start.plusHours(2);
        when(repository.existsOverlappingMWindow(5L, start, end)).thenReturn(true);

        OverlapException exception = assertThrows(OverlapException.class,
                () -> validator.checkForOverlap(elements, start, end));

        assertEquals("Overlap detected: maintenance window already exists for element 'Router-A'",
                exception.getMessage());
    }

    @Test
    void checkForOverlapTest() {
        NetworkElementEntity first = new NetworkElementEntity();
        first.setId(5L);
        first.setName("Router-A");

        NetworkElementEntity second = new NetworkElementEntity();
        second.setId(6L);
        second.setName("Switch-B");

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0);
        LocalDateTime end = start.plusHours(1);

        when(repository.existsOverlappingMWindow(5L, start, end)).thenReturn(false);
        when(repository.existsOverlappingMWindow(6L, start, end)).thenReturn(false);

        assertDoesNotThrow(() -> validator.checkForOverlap(List.of(first, second), start, end));

        verify(repository).existsOverlappingMWindow(5L, start, end);
        verify(repository).existsOverlappingMWindow(6L, start, end);
    }
}
