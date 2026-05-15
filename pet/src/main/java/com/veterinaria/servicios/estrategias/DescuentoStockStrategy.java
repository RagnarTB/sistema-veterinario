package com.veterinaria.servicios.estrategias;

import java.math.BigDecimal;
import com.veterinaria.modelos.Producto;
import com.veterinaria.modelos.Sede;

public interface DescuentoStockStrategy {
    void descontar(Producto producto, Sede sede, BigDecimal cantidad);
}
