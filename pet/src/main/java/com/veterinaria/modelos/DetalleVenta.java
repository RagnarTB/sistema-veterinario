package com.veterinaria.modelos;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "detalles_venta")
@Data
@NoArgsConstructor
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer cantidad;

    @Column(precision = 19, scale = 2)
    private BigDecimal precioUnitario;

    @Column(precision = 19, scale = 2)
    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;

    // Un detalle puede ser un producto físico con stock
    @ManyToOne
    @JoinColumn(name = "producto_id") // nullable por defecto: puede ser null si el ítem es un servicio
    private Producto producto;

    // O puede ser un servicio médico (consulta, corte de pelo, etc.)
    @ManyToOne
    @JoinColumn(name = "servicio_medico_id") // nullable: puede ser null si el ítem es un producto
    private ServicioMedico servicio;
}