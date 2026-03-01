package com.tus.maintainx.controller;


import com.tus.maintainx.dto.NetworkElementCreateDTO;
import com.tus.maintainx.dto.NetworkElementResponseDTO;
import com.tus.maintainx.service.NetworkElementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/network-elements")
@RequiredArgsConstructor
public class NetworkElementController {

    private final NetworkElementService networkElementService;

    @PostMapping
    public ResponseEntity<NetworkElementResponseDTO> create(@Valid @RequestBody NetworkElementCreateDTO dto) {

        NetworkElementResponseDTO created = networkElementService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<NetworkElementResponseDTO>> getAll() {
        return ResponseEntity.ok(networkElementService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NetworkElementResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(networkElementService.getByElementId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NetworkElementResponseDTO> update(@PathVariable Long id, @Valid @RequestBody NetworkElementCreateDTO dto) {
        return ResponseEntity.ok(networkElementService.update(id, dto));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<NetworkElementResponseDTO> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(networkElementService.deactivate(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<NetworkElementResponseDTO> activate(@PathVariable Long id) {
        return ResponseEntity.ok(networkElementService.activate(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        networkElementService.delete(id);
    }

}
