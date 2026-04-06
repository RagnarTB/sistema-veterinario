package com.veterinaria.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DetalleVentaRequestDTO {

    // Opcional individualmente: el frontend manda UNO de los dos.
    // Si viene ninguno o ambos, el servicio lanza BAD_REQUEST.
    private Long productoId;

    private Long servicioId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;
}