package com.tus.maintainx.controller;


import com.tus.maintainx.dto.NetworkElementCreateDTO;
import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.service.NetworkElementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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



}
