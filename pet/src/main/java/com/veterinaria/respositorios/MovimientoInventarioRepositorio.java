package com.veterinaria.respositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.veterinaria.modelos.MovimientoInventario;

public interface MovimientoInventarioRepositorio extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByProductoIdAndSedeIdOrderByFechaDesc(Long productoId, Long sedeId);
}
