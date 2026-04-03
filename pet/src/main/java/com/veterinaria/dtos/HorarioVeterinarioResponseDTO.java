package com.veterinaria.dtos;

import java.time.DayOfWeek;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HorarioVeterinarioResponseDTO {
    private Long id;
    private Long veterinarioId;
    private DayOfWeek diaSemana;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private LocalTime inicioRefrigerio;
    private LocalTime finRefrigerio;
}