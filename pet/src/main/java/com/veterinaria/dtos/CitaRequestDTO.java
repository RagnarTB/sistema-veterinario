package com.veterinaria.dtos;

import java.time.LocalDate;
import java.time.LocalTime;

import com.veterinaria.modelos.EstadoCita;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CitaRequestDTO {

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;
    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;
    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;
    @NotNull(message = "El ID del paciente es obligatorio")
    private Long pacienteId;

}
