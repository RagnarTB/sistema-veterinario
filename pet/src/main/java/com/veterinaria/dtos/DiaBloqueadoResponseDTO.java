package com.veterinaria.dtos;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiaBloqueadoResponseDTO {
    private Long id;
    private LocalDate fecha;
    private String motivo;
    private Long veterinarioId; // Será null si es un feriado general
}