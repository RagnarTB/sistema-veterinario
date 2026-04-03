package com.veterinaria.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // Asumiendo que usas Lombok para ahorrarte los getters y setters
public class EspecieRequestDTO {

    @NotBlank(message = "El nombre de la especie no puede estar vacío")
    private String nombre;

}