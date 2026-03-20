/**
 * REST controller for network element.
 * Handles API requests for network element.
 */

package com.tus.maintainx.controller;

import com.tus.maintainx.dto.NetworkElementCreateDTO;
import com.tus.maintainx.dto.NetworkElementResponseDTO;
import com.tus.maintainx.service.NetworkElementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/network-elements")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Network Elements", description = "APIs for managing network elements")
public class NetworkElementController {

    private final NetworkElementService networkElementService;

    @Operation(summary = "Create network element", description = "Creates a new network element")
    @PostMapping
    public ResponseEntity<NetworkElementResponseDTO> create(@Valid @RequestBody NetworkElementCreateDTO dto) {
        NetworkElementResponseDTO created = networkElementService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get all network elements", description = "Returns all network elements")
    @GetMapping
    public ResponseEntity<List<NetworkElementResponseDTO>> getAll() {
        return ResponseEntity.ok(networkElementService.getAll());
    }

    @Operation(summary = "Get network element by id", description = "Returns one network element by id")
    @GetMapping("/{id}")
    public ResponseEntity<NetworkElementResponseDTO> getById(
            @Parameter(description = "Network element id")
            @PathVariable Long id) {
        return ResponseEntity.ok(networkElementService.getByElementId(id));
    }

    @Operation(summary = "Update network element", description = "Updates an existing network element")
    @PutMapping("/{id}")
    public ResponseEntity<NetworkElementResponseDTO> update(
            @Parameter(description = "Network element id")
            @PathVariable Long id,
            @Valid @RequestBody NetworkElementCreateDTO dto) {
        return ResponseEntity.ok(networkElementService.update(id, dto));
    }

    @Operation(summary = "Deactivate network element", description = "Marks a network element as inactive")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<NetworkElementResponseDTO> deactivate(
            @Parameter(description = "Network element id")
            @PathVariable Long id) {
        return ResponseEntity.ok(networkElementService.deactivate(id));
    }

    @Operation(summary = "Activate network element", description = "Marks a network element as active")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<NetworkElementResponseDTO> activate(
            @Parameter(description = "Network element id")
            @PathVariable Long id) {
        return ResponseEntity.ok(networkElementService.activate(id));
    }

    @Operation(summary = "Delete network element", description = "Deletes a network element by id")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "Network element id")
            @PathVariable Long id) {
        networkElementService.delete(id);
    }
}