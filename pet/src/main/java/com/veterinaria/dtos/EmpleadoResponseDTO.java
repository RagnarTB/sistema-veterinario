package com.veterinaria.dtos;

import java.math.BigDecimal;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoResponseDTO {
    private Long id;
    private Long usuarioId;
    private String email;
    private Set<String> nombresRoles;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String especialidad;
    private String numeroColegiatura;
    private BigDecimal sueldoBase;
    private Boolean activo;
    private Set<Long> sedeIds;
    private Set<String> sedeNombres;
}
