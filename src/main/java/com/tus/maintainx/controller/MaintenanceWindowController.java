package com.tus.maintainx.controller;

import com.tus.maintainx.dto.MaintenanceWindowCreateRequestDTO;
import com.tus.maintainx.dto.MaintenanceWindowResponseDTO;
import com.tus.maintainx.service.MaintenanceWindowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
