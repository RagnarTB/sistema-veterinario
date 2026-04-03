package com.veterinaria.dtos;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HorarioVeterinarioRequestDTO {

    @NotNull(message = "El ID del veterinario es obligatorio")
    private Long veterinarioId;

    @NotNull(message = "El día de la semana es obligatorio")
    private DayOfWeek diaSemana; // Ej: MONDAY, TUESDAY

    @NotNull(message = "La hora de entrada es obligatoria")
    private LocalTime horaEntrada;

    @NotNull(message = "La hora de salida es obligatoria")
    private LocalTime horaSalida;

    // Estos son opcionales (el doctor podría no tener hora de refrigerio)
    private LocalTime inicioRefrigerio;
    private LocalTime finRefrigerio;
}