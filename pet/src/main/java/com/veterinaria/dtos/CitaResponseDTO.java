package com.veterinaria.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List; // ¡No olvides este import!

import com.veterinaria.modelos.Enums.EstadoCita;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CitaResponseDTO {
    private Long id;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private EstadoCita estado;
    // EL CAMBIO
    private List<Long> pacienteIds;
}