package com.veterinaria.modelos;

import java.time.LocalDateTime;

import com.veterinaria.modelos.Enums.TipoMovimiento;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movimiento_caja")
public class MovimientoCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String concepto;
    private Double monto;
    private TipoMovimiento tipoMovimiento;
    private LocalDateTime fechaHora;
    @ManyToOne
    @JoinColumn(name = "caja_diaria_id", nullable = false)
    private CajaDiaria cajaDiaria;
}
