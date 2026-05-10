package com.veterinaria.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SeleccionarRolRequestDTO {
    @NotBlank(message = "El rol es obligatorio")
    private String rol;
}
