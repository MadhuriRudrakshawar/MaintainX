package com.tus.maintainx.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class NetworkElementCreateDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String elementType;

    @NotBlank
    private String region;

    @NotBlank
    private String status;

}
