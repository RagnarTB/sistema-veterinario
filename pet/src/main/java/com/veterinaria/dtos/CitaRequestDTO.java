package com.veterinaria.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List; // ¡No olvides este import!

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CitaRequestDTO {

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    private LocalTime horaInicio;

    @NotNull
    private Long servicioId;
    @NotNull
    private Long veterinarioId;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    // ¡EL CAMBIO! Pasamos de un Long a una List<Long>
    @NotEmpty(message = "La cita debe tener al menos un paciente")
    private List<Long> pacienteIds;

}