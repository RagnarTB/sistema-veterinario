package com.veterinaria.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitasVeterinarioDTO {

    private String emailVeterinario;
    private Long totalCitas;
}
