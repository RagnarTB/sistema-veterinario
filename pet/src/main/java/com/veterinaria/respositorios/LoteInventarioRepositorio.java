package com.veterinaria.respositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.veterinaria.modelos.LoteInventario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoteInventarioRepositorio extends JpaRepository<LoteInventario, Long> {
    
    @Query("SELECT l FROM LoteInventario l WHERE l.producto.id = :productoId AND l.sede.id = :sedeId AND l.activo = true ORDER BY l.fechaVencimiento ASC NULLS LAST")
    List<LoteInventario> findLotesParaFIFO(@Param("productoId") Long productoId, @Param("sedeId") Long sedeId);
    
    // Para devoluciones (anulaciones): devolver al lote más nuevo o crear uno si no hay
    List<LoteInventario> findByProductoIdAndSedeIdOrderByFechaVencimientoDesc(Long productoId, Long sedeId);

    boolean existsByProveedorId(Long proveedorId);
}
