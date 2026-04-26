package com.veterinaria.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfirmarTokenRequestDTO {
    
    @NotBlank(message = "El token es obligatorio")
    private String token;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
