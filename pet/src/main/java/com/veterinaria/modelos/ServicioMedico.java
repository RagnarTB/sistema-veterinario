package com.veterinaria.modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "servicios_medicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private Integer duracionMinutos; // Ej: 30, 45, 60

    @Column(nullable = false)
    private Integer bufferMinutos = 0; // Tiempo extra para limpiar consultorio. Ej: 10

    private Boolean activo = true; // Para no borrar servicios (Soft Delete)
}