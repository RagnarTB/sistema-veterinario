package com.veterinaria.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IngresoStockDTO {
    
    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La sede es obligatoria")
    private Long sedeId;

    private Long proveedorId;

    @NotBlank(message = "El número de lote es obligatorio")
    private String numeroLote;

    private LocalDate fechaVencimiento;

    @NotNull(message = "La cantidad comprada es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    private BigDecimal cantidadComprada;

    private String motivo;
}
