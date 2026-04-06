package com.veterinaria.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetalleVentaResponseDTO {

    // Uno de los dos tendrá valor; el otro será null.
    private Long productoId;
    private Long servicioId;

    // Nombre del producto O del servicio, dependiendo del tipo de ítem
    private String nombreItem;

    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}