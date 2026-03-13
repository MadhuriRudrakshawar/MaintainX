package com.tus.maintainx.service;


import com.tus.maintainx.dto.MaintenanceWindowCreateRequestDTO;
import com.tus.maintainx.dto.MaintenanceWindowResponseDTO;
import com.tus.maintainx.dto.MaintenanceWindowUpdateRequestDTO;
import com.tus.maintainx.entity.MaintenanceWindowEntity;
import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.entity.UserEntity;
import com.tus.maintainx.enums.AuditAction;
import com.tus.maintainx.exception.BadRequestException;
import com.tus.maintainx.exception.NotFoundException;
import com.tus.maintainx.exception.OverlapException;
import com.tus.maintainx.repository.MaintenanceWindowRepository;
import com.tus.maintainx.repository.NetworkElementRepository;
import com.tus.maintainx.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceWindowService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String WINDOW_NOT_FOUND = "Maintenance Window not found: ";

    private final MaintenanceWindowRepository maintenanceWindowRepository;
    private final NetworkElementRepository networkElementRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public MaintenanceWindowResponseDTO create(@Valid MaintenanceWindowCreateRequestDTO dto) {

        String username = getAuthenticatedUsername();

        UserEntity currentUser = userRepository.findByUsername(username);
        if (currentUser == null) {
            throw new NotFoundException("User not found: " + username);
        }

        List<NetworkElementEntity> elements =
                networkElementRepository.findAllById(dto.getNetworkElementIds());

        if (elements.size() != dto.getNetworkElementIds().size()) {
            throw new BadRequestException("Some Network Element IDs are invalid");
        }

        MaintenanceWindowEntity e = new MaintenanceWindowEntity();
        e.setTitle(dto.getTitle());
        e.setDescription(dto.getDescription());
        e.setStartTime(dto.getStartTime());
        e.setEndTime(dto.getEndTime());
        e.setWindowStatus(STATUS_PENDING);
        e.setDecidedBy(STATUS_PENDING);
        e.setRequestedBy(currentUser);
        e.getNetworkElements().clear();
        e.getNetworkElements().addAll(elements);


        for (Long elementId : dto.getNetworkElementIds()) {
            boolean overlap = maintenanceWindowRepository.existsOverlappingMWindow(
                    elementId,
                    dto.getStartTime(),
                    dto.getEndTime()
            );

            if (overlap) {

                String name = "Unknown";
                for (NetworkElementEntity ne : elements) {
                    if (ne.getId().equals(elementId)) {
                        name = ne.getName();
                    }
                }

                throw new OverlapException(
                        "Overlap detected: maintenance window already exists for element '" + name + "'"
                );
            }
        }

        MaintenanceWindowEntity saved = maintenanceWindowRepository.save(e);
        auditService.log(saved.getId(), AuditAction.CREATED, "Maintenance window created");
        return toResponse(saved);
    }


    public List<MaintenanceWindowResponseDTO> getAll() {
        List<MaintenanceWindowEntity> list = maintenanceWindowRepository.findAll();
        List<MaintenanceWindowResponseDTO> result = new java.util.ArrayList<>();

        for (MaintenanceWindowEntity e : list) {
            result.add(toResponse(e));
        }
        return result;
    }

    public MaintenanceWindowResponseDTO getById(Long id) {
        MaintenanceWindowEntity e = maintenanceWindowRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(WINDOW_NOT_FOUND + id));
        return toResponse(e);
    }

    @Transactional
    public MaintenanceWindowResponseDTO update(Long id, MaintenanceWindowUpdateRequestDTO dto) {


        MaintenanceWindowEntity e = maintenanceWindowRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(WINDOW_NOT_FOUND + id));


        List<NetworkElementEntity> elements =
                networkElementRepository.findAllById(dto.getNetworkElementIds());

        if (elements.size() != dto.getNetworkElementIds().size()) {
            throw new BadRequestException("Some Network Element IDs are invalid");
        }

        e.setTitle(dto.getTitle());
        e.setDescription(dto.getDescription());
        e.setStartTime(dto.getStartTime());
        e.setEndTime(dto.getEndTime());
        if (e.getDecidedBy() == null || e.getDecidedBy().isBlank()) {
            e.setDecidedBy(STATUS_PENDING);
        }
        e.getNetworkElements().clear();
        e.getNetworkElements().addAll(elements);

        MaintenanceWindowEntity saved = maintenanceWindowRepository.save(e);
        auditService.log(saved.getId(), AuditAction.UPDATED, "Maintenance window updated");
        return toResponse(saved);
    }


    private MaintenanceWindowResponseDTO toResponse(MaintenanceWindowEntity e) {
        List<NetworkElementEntity> sortedElements = e.getNetworkElements().stream()
                .sorted(Comparator.comparing(NetworkElementEntity::getId))
                .toList();
        List<Long> ids = sortedElements.stream().map(NetworkElementEntity::getId).toList();
        List<String> codes = sortedElements.stream().map(NetworkElementEntity::getElementCode).toList();
        List<String> names = sortedElements.stream().map(NetworkElementEntity::getName).toList();
        String requestedByUsername = e.getRequestedBy() == null ? null : e.getRequestedBy().getUsername();

        return MaintenanceWindowResponseDTO.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .windowStatus(e.getWindowStatus())
                .executionStatus(e.getExecutionStatus() == null ? null : e.getExecutionStatus().name())
                .requestedByUsername(requestedByUsername)
                .rejectionReason(e.getRejectionReason())
                .decidedBy(e.getDecidedBy())
                .networkElementIds(ids)
                .networkElementCodes(codes)
                .networkElementNames(names)
                .build();
    }

    @Transactional
    public void delete(Long id) {
        if (!maintenanceWindowRepository.existsById(id)) {
            throw new NotFoundException(WINDOW_NOT_FOUND + id);
        }
        auditService.log(id, AuditAction.CANCELLED, "Maintenance window deleted");
        maintenanceWindowRepository.deleteById(id);

    }

    @Transactional
    public MaintenanceWindowResponseDTO approve(Long id) {
        MaintenanceWindowEntity e = maintenanceWindowRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(WINDOW_NOT_FOUND + id));

        if (!STATUS_PENDING.equalsIgnoreCase(e.getWindowStatus())) {
            throw new BadRequestException("Only PENDING requests can be approved");
        }

        String approver = getAuthenticatedUsername();

        e.setWindowStatus(STATUS_APPROVED);
        e.setRejectionReason(null);
        e.setDecidedBy(approver);

        MaintenanceWindowEntity saved = maintenanceWindowRepository.save(e);
        auditService.log(saved.getId(), AuditAction.APPROVED, "Maintenance window approved");
        return toResponse(saved);
    }

    @Transactional
    public MaintenanceWindowResponseDTO reject(Long id, String reason) {
        MaintenanceWindowEntity e = maintenanceWindowRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(WINDOW_NOT_FOUND + id));

        if (!STATUS_PENDING.equalsIgnoreCase(e.getWindowStatus())) {
            throw new BadRequestException("Only PENDING requests can be rejected");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new BadRequestException("Rejection reason is required");
        }

        String approver = getAuthenticatedUsername();

        e.setWindowStatus(STATUS_REJECTED);
        e.setRejectionReason(reason.trim());
        e.setDecidedBy(approver);

        MaintenanceWindowEntity saved = maintenanceWindowRepository.save(e);
        auditService.log(saved.getId(), AuditAction.REJECTED, "Maintenance window rejected: " + reason.trim());
        return toResponse(saved);
    }

    private String getAuthenticatedUsername() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        if (username == null || username.isBlank()) {
            throw new BadRequestException("Authenticated user not found");
        }
        return username;
    }
}
