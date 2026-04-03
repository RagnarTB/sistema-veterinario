package com.veterinaria.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServicioMedicoRequestDTO {

    @NotBlank(message = "El nombre del servicio es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double precio;

    @NotNull(message = "La duración es obligatoria")
    @Min(value = 1, message = "La duración mínima es de 1 minuto")
    private Integer duracionMinutos;

    @NotNull(message = "El tiempo de limpieza (buffer) es obligatorio")
    @Min(value = 0, message = "El buffer no puede ser negativo")
    private Integer bufferMinutos;
}