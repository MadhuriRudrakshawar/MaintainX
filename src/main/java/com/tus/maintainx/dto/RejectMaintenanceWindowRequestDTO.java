package com.tus.maintainx.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectMaintenanceWindowRequestDTO {
    @NotBlank
    private String reason;
}