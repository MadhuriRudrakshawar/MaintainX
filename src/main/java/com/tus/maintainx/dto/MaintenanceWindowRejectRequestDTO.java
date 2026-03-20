/**
 * DTO class for maintenance window reject.
 * Carries data between API layers.
 */

package com.tus.maintainx.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MaintenanceWindowRejectRequestDTO {
    @NotBlank
    private String reason;
}