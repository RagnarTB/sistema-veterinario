package com.veterinaria.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DiaBloqueadoRequestDTO {

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotBlank(message = "El motivo es obligatorio (Ej: Feriado, Vacaciones, Fumigación)")
    private String motivo;

    // Es opcional. Si viene nulo, significa que TODA la clínica cierra.
    // Si viene con un ID, significa que solo ese doctor tiene el día libre.
    private Long veterinarioId;
}