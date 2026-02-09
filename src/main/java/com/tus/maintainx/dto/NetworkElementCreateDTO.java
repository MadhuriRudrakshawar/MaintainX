package com.tus.maintainx.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class NetworkElementCreateDTO {

    @NotBlank
    private String elementCode;

    @NotBlank
    private String name;

    @NotNull
    private String elementType;

    @NotBlank
    private String region;

}
