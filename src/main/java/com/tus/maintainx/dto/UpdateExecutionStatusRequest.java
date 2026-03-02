package com.tus.maintainx.dto;


import com.tus.maintainx.enums.ExecutionStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateExecutionStatusRequest(
        @NotNull ExecutionStatus status
) {
}