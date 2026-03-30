package com.veterinaria.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AtencionMedicaRequestDTO {

    @NotNull(message = "el id de la cita es obligatorio")
    private Long citaId;
    @NotBlank(message = "los sintomas son obligatorios")
    private String sintomas;
    @NotBlank(message = "el diagnostico es obligatorio")
    private String diagnostico;
    @NotBlank(message = "el tratamiento es obligatorio")
    private String tratamiento;
    @NotNull(message = "el peso es obligatorio")
    private Double peso;
    @NotNull(message = "la temperatura es obligatoria")
    private Double temperatura;
    @NotNull(message = "la frecuencia cardiaca es obligatoria")
    private Integer frecuenciaCardiaca;

}
