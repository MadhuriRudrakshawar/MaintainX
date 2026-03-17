package com.tus.maintainx.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MaintenanceWindowRejectRequestDTO {
    @NotBlank
    private String reason;
}