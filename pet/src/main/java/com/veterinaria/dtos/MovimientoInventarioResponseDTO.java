package com.veterinaria.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovimientoInventarioResponseDTO {
    private Long id;
    private String tipoMovimiento;
    private BigDecimal cantidad;
    private String motivo;
    private LocalDateTime fecha;
    private String responsableNombre;
}
