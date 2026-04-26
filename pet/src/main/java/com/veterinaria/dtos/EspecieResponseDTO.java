package com.veterinaria.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EspecieResponseDTO {
    private Long id;
    private String nombre;
    private Boolean activo;
}