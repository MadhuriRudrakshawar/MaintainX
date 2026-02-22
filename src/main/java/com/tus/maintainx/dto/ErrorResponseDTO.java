package com.tus.maintainx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {
    private Instant timestamp;
    private int status;
    private String code;
    private String message;
    private String path;
    private Map<String, String> details;
}