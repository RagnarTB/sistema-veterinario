package com.veterinaria.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TrasladoHospitalizacionDTO {
    @NotNull(message = "El ID de la nueva jaula es obligatorio")
    @Positive(message = "El ID de la nueva jaula debe ser positivo")
    private Long nuevaJaulaId;
}
