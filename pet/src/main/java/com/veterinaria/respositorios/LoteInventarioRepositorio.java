package com.veterinaria.respositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.veterinaria.modelos.LoteInventario;

public interface LoteInventarioRepositorio extends JpaRepository<LoteInventario, Long> {
    List<LoteInventario> findByProductoIdAndSedeIdAndActivoTrueOrderByFechaVencimientoAsc(Long productoId, Long sedeId);
}
