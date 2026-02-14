package com.tus.maintainx.controller;


import com.tus.maintainx.dto.NetworkElementCreateDTO;
import com.tus.maintainx.entity.NetworkElementEntity;
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
    public ResponseEntity<NetworkElementEntity> create(@Valid @RequestBody NetworkElementCreateDTO dto){

        NetworkElementEntity created = networkElementService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<NetworkElementEntity>> getAll(){
        return ResponseEntity.ok(networkElementService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NetworkElementEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(networkElementService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<NetworkElementEntity> update(@PathVariable Long id, @Valid @RequestBody NetworkElementCreateDTO dto){

        return ResponseEntity.ok(networkElementService.update(id, dto));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<NetworkElementEntity> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(networkElementService.deactivate(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<NetworkElementEntity> activate(@PathVariable Long id) {
        return ResponseEntity.ok(networkElementService.activate(id));
    }





}
