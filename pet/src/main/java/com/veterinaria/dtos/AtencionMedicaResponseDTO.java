package com.veterinaria.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtencionMedicaResponseDTO {
    private Long id;
    private String sintomas;
    private String diagnostico;
    private String tratamiento;
    private Double peso;
    private Double temperatura;
    private Integer frecuenciaCardiaca;
    private String resumenIaCliente;
    private Long citaId;

}
