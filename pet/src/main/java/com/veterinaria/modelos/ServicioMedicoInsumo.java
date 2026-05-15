package com.veterinaria.modelos;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "servicios_medicos_insumos",
       uniqueConstraints = @UniqueConstraint(name = "uk_servicio_producto_activo", 
                                            columnNames = {"servicio_medico_id", "producto_id", "activo"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioMedicoInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_medico_id", nullable = false)
    private ServicioMedico servicioMedico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal cantidadEstimada;

    private String notas;

    private Boolean activo = true;
}
