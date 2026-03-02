package com.tus.maintainx.controller;

import com.tus.maintainx.dto.*;
import com.tus.maintainx.entity.MaintenanceWindowEntity;
import com.tus.maintainx.service.MaintenanceWindowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance-windows")
@RequiredArgsConstructor
public class MaintenanceWindowController {

    private final MaintenanceWindowService service;

    @PostMapping
    public ResponseEntity<MaintenanceWindowResponseDTO> create(@RequestBody MaintenanceWindowCreateRequestDTO dto) {
        MaintenanceWindowResponseDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);

    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceWindowResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }


    @GetMapping
    public ResponseEntity<List<MaintenanceWindowResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceWindowResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceWindowUpdateRequestDTO dto) {

        return ResponseEntity.ok(service.update(id, dto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<MaintenanceWindowResponseDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(service.approve(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<MaintenanceWindowResponseDTO> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectMaintenanceWindowRequestDTO dto) {

        return ResponseEntity.ok(service.reject(id, dto.getReason()));
    }

    @PatchMapping("/{id}/execution-status")
    @PreAuthorize("hasRole('ENGINEER')")
    public ResponseEntity<MaintenanceWindowEntity> updateExecutionStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExecutionStatusRequest req
    ) {
        return ResponseEntity.ok(service.updateExecutionStatus(id, req.status()));
    }
}
