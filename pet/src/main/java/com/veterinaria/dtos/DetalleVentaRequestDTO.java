package com.veterinaria.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DetalleVentaRequestDTO {

    // Opcional individualmente: el frontend manda UNO de los dos.
    // Si viene ninguno o ambos, el servicio lanza BAD_REQUEST.
    private Long productoId;

    private Long servicioId;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", inclusive = true, message = "La cantidad debe ser mayor a 0")
    private BigDecimal cantidad;
}