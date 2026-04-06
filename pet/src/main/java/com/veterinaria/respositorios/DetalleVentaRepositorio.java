package com.veterinaria.respositorios;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.veterinaria.dtos.TopProductoDTO;
import com.veterinaria.modelos.DetalleVenta;

public interface DetalleVentaRepositorio extends JpaRepository<DetalleVenta, Long> {

    // Top N productos más vendidos (solo de ventas activas, agrupados por producto)
    @Query("SELECT new com.veterinaria.dtos.TopProductoDTO(p.nombre, SUM(d.cantidad)) " +
            "FROM DetalleVenta d " +
            "JOIN d.producto p " +
            "JOIN d.venta v " +
            "WHERE v.estado = com.veterinaria.modelos.Enums.EstadoVenta.ACTIVA " +
            "GROUP BY p.id, p.nombre " +
            "ORDER BY SUM(d.cantidad) DESC")
    List<TopProductoDTO> findTopProductos(Pageable pageable);
}
