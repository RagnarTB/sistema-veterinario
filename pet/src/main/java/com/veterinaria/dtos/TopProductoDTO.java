package com.veterinaria.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductoDTO {

    private String nombreProducto;

    // SUM(d.cantidad) retorna BigDecimal ahora que cantidad es BigDecimal
    private BigDecimal cantidadVendida;
}
