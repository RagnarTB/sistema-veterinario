package com.veterinaria.servicios.estrategias;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.veterinaria.modelos.LoteInventario;
import com.veterinaria.modelos.Producto;
import com.veterinaria.modelos.Sede;
import com.veterinaria.respositorios.LoteInventarioRepositorio;

@Component
public class FifoDescuentoStrategy implements DescuentoStockStrategy {

    private final LoteInventarioRepositorio loteRepositorio;

    public FifoDescuentoStrategy(LoteInventarioRepositorio loteRepositorio) {
        this.loteRepositorio = loteRepositorio;
    }

    @Override
    public void descontar(Producto producto, Sede sede, BigDecimal cantidad) {
        List<LoteInventario> lotes = loteRepositorio.findLotesParaFIFO(producto.getId(), sede.getId());
        BigDecimal restante = cantidad;
        for (LoteInventario lote : lotes) {
            if (restante.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal disponible = lote.getStockRestante();
            if (disponible.compareTo(restante) >= 0) {
                lote.setStockRestante(disponible.subtract(restante));
                restante = BigDecimal.ZERO;
            } else {
                restante = restante.subtract(disponible);
                lote.setStockRestante(BigDecimal.ZERO);
                lote.setActivo(false);
            }
            loteRepositorio.save(lote);
        }
    }
}
