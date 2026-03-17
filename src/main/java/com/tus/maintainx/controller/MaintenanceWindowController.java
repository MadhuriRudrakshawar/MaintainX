package com.tus.maintainx.controller;

import com.tus.maintainx.dto.MaintenanceWindowCreateRequestDTO;
import com.tus.maintainx.dto.MaintenanceWindowRejectRequestDTO;
import com.tus.maintainx.dto.MaintenanceWindowResponseDTO;
import com.tus.maintainx.dto.MaintenanceWindowUpdateRequestDTO;
import com.tus.maintainx.service.MaintenanceWindowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance-windows")
@RequiredArgsConstructor
@Tag(name = "Maintenance Windows", description = "APIs for managing maintenance windows")
public class MaintenanceWindowController {

    private final MaintenanceWindowService service;

    @Operation(summary = "Create maintenance window", description = "Creates a new maintenance window")
    @PostMapping
    public ResponseEntity<MaintenanceWindowResponseDTO> create(@Valid @RequestBody MaintenanceWindowCreateRequestDTO dto) {
        MaintenanceWindowResponseDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get maintenance window by id", description = "Returns one maintenance window by id")
    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceWindowResponseDTO> getById(
            @Parameter(description = "Maintenance window id")
            @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Get all maintenance windows", description = "Returns all maintenance windows")
    @GetMapping
    public ResponseEntity<List<MaintenanceWindowResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(summary = "Update maintenance window", description = "Updates an existing maintenance window")
    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceWindowResponseDTO> update(
            @Parameter(description = "Maintenance window id")
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceWindowUpdateRequestDTO dto) {

        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Delete maintenance window", description = "Deletes a maintenance window by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Maintenance window id")
            @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Approve maintenance window", description = "Approves a maintenance window request")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<MaintenanceWindowResponseDTO> approve(
            @Parameter(description = "Maintenance window id")
            @PathVariable Long id) {
        return ResponseEntity.ok(service.approve(id));
    }

    @Operation(summary = "Reject maintenance window", description = "Rejects a maintenance window request with reason")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<MaintenanceWindowResponseDTO> reject(
            @Parameter(description = "Maintenance window id")
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceWindowRejectRequestDTO dto) {

        return ResponseEntity.ok(service.reject(id, dto.getReason()));
    }
}
