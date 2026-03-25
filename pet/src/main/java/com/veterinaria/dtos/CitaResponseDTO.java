package com.veterinaria.dtos;

import java.time.LocalDate;
import java.time.LocalTime;

import com.veterinaria.modelos.EstadoCita;
import com.veterinaria.modelos.Paciente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CitaResponseDTO {
    private Long id;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private EstadoCita estado;
    private Long pacienteId;

}
