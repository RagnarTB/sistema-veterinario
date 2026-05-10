package com.veterinaria.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoteInventarioResponseDTO {
    private Long id;
    private String numeroLote;
    private LocalDate fechaVencimiento;
    private BigDecimal stockRestante;
    private String proveedorNombre;
}
