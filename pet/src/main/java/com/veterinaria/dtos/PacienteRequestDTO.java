package com.veterinaria.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PacienteRequestDTO {
    @NotBlank(message = "El nombre del paciente es obligatorio")
    private String nombre;

    // ¡EL CAMBIO! Extirpamos el String y pedimos el ID (NotNull porque es número,
    // no texto)
    @NotNull(message = "El ID de la especie es obligatorio")
    private Long especieId;

    private String raza;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;
}
