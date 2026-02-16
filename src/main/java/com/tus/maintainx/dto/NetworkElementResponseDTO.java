package com.tus.maintainx.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NetworkElementResponseDTO {

    private Long id;

    private String elementCode;

    private String name;

    private String elementType;

    private String region;

    private String status;


}
