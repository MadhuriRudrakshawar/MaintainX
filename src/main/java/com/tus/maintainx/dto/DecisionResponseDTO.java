package com.tus.maintainx.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DecisionResponseDTO {
    private Long id;
    private String windowStatus;
    private String rejectionReason;
    private String decidedBy;
    private String decidedAt;
}