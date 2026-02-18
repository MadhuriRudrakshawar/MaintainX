package com.tus.maintainx.controller;

import com.tus.maintainx.dto.MaintenanceWindowCreateRequestDTO;
import com.tus.maintainx.dto.MaintenanceWindowResponseDTO;
import com.tus.maintainx.service.MaintenanceWindowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/maintenance-windows")
@RequiredArgsConstructor
public class MaintenanceWindowController {

    private final MaintenanceWindowService service;

    @PostMapping
    public ResponseEntity<MaintenanceWindowResponseDTO> create(
            @Valid @RequestBody MaintenanceWindowCreateRequestDTO dto) {

        MaintenanceWindowResponseDTO created = service.create(dto);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
