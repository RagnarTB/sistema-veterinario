package com.veterinaria.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String telefono;
    private String dni;
    private String email;
    private Boolean activo;
    private Boolean verificado;

}
