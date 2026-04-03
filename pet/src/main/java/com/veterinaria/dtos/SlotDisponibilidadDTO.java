package com.veterinaria.dtos;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlotDisponibilidadDTO {
    private LocalTime horaInicio;
    private LocalTime horaFin;
}