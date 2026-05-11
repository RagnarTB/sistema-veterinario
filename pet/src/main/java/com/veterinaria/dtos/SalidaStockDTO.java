package com.veterinaria.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SalidaStockDTO {

    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La sede es obligatoria")
    private Long sedeId;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    private BigDecimal cantidad;

    @NotBlank(message = "El tipo de movimiento es obligatorio")
    private String tipoMovimiento; // SALIDA_CONSUMO_INTERNO, AJUSTE_NEGATIVO, MERMA_VENCIMIENTO, AJUSTE_POSITIVO

    private String motivo;
}
