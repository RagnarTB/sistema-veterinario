package com.veterinaria.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequestDTO {
    @NotBlank(message = "El token de Google es obligatorio")
    private String idToken;
}
