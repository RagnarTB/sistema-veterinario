package com.veterinaria.dtos;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmpleadoRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;
    // Eliminado el password ya que se creará por correo electrónico

    @NotEmpty(message = "Debe asignar al menos un rol al empleado")
    private Set<String> roles;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener exactamente 8 caracteres")
    private String dni;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    private String especialidad;

    private BigDecimal sueldoBase;

    @NotEmpty(message = "Debe asignar al menos una sede al empleado")
    private Set<Long> sedeIds;
}
