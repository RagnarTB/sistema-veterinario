package com.veterinaria.modelos;

import java.math.BigDecimal;

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

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer duracionMinutos; // Ej: 30, 45, 60

    @Column(nullable = false)
    private Integer bufferMinutos = 0; // Tiempo extra para limpiar consultorio. Ej: 10

    @Enumerated(EnumType.STRING)
    private TipoServicio tipoServicio;

    private Boolean activo = true; // Para no borrar servicios (Soft Delete)
}