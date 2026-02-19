package com.tus.maintainx.service;


import com.tus.maintainx.dto.MaintenanceWindowCreateRequestDTO;
import com.tus.maintainx.dto.MaintenanceWindowResponseDTO;
import com.tus.maintainx.dto.MaintenanceWindowUpdateRequestDTO;
import com.tus.maintainx.entity.MaintenanceWindowEntity;
import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.entity.UserEntity;
import com.tus.maintainx.exception.BadRequestException;
import com.tus.maintainx.exception.NotFoundException;
import com.tus.maintainx.exception.OverlapException;
import com.tus.maintainx.repository.MaintenanceWindowRepository;
import com.tus.maintainx.repository.NetworkElementRepository;
import com.tus.maintainx.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceWindowService {


    private final MaintenanceWindowRepository maintenanceWindowRepository;
    private final NetworkElementRepository networkElementRepository;
    private final UserRepository userRepository;

    @Transactional
    public MaintenanceWindowResponseDTO create(@Valid MaintenanceWindowCreateRequestDTO dto) {

        UserEntity currentUser = userRepository.findById(dto.getRequestedById())
                .orElseThrow(() -> new NotFoundException("User not found: " + dto.getRequestedById()));


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
        e.setWindowStatus("PENDING");
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
                throw new OverlapException(
                        "Overlap detected: maintenance window already exists for elementId=" + elementId
                );
            }
        }

        MaintenanceWindowEntity saved = maintenanceWindowRepository.save(e);
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
                .orElseThrow(() -> new NotFoundException("Maintenance Window not found: " + id));
        return toResponse(e);
    }

    @Transactional
    public MaintenanceWindowResponseDTO update(Long id, MaintenanceWindowUpdateRequestDTO dto) {


        MaintenanceWindowEntity e = maintenanceWindowRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Maintenance Window not found: " + id));


        List<NetworkElementEntity> elements =
                networkElementRepository.findAllById(dto.getNetworkElementIds());

        if (elements.size() != dto.getNetworkElementIds().size()) {
            throw new BadRequestException("Some Network Element IDs are invalid");
        }

        e.setTitle(dto.getTitle());
        e.setDescription(dto.getDescription());
        e.setStartTime(dto.getStartTime());
        e.setEndTime(dto.getEndTime());
        e.getNetworkElements().clear();
        e.getNetworkElements().addAll(elements);

        MaintenanceWindowEntity saved = maintenanceWindowRepository.save(e);
        return toResponse(saved);
    }


    private MaintenanceWindowResponseDTO toResponse(MaintenanceWindowEntity e) {
        List<Long> ids = e.getNetworkElements().stream().map(NetworkElementEntity::getId).toList();
        List<String> names = e.getNetworkElements().stream().map(NetworkElementEntity::getName).toList();

        return MaintenanceWindowResponseDTO.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .windowStatus(e.getWindowStatus())
                .requestedByUsername(e.getRequestedBy().getUsername())
                .networkElementIds(ids)
                .networkElementNames(names)
                .build();
    }

    @Transactional
    public void delete(Long id) {
        if (!maintenanceWindowRepository.existsById(id)) {
            throw new NotFoundException("Maintenance Window not found: " + id);
        }
        maintenanceWindowRepository.deleteById(id);
    }
}
