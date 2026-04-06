package com.veterinaria.modelos;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal precio;

    private Integer stockActual;

    // Stock mínimo para alertas de inventario. Valor por defecto 5 para no
    // romper registros existentes en la base de datos.
    @Column(nullable = false)
    private Integer stockMinimo = 5;

    @Version
    private Long version;

    @Column(nullable = false)
    private Boolean activo = true;
}
