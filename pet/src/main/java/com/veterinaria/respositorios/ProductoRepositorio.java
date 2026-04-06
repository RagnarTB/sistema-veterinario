package com.veterinaria.respositorios;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.veterinaria.modelos.Producto;

public interface ProductoRepositorio extends JpaRepository<Producto, Long> {

    // Para el módulo de "Nueva Venta": solo productos activos con paginación
    Page<Producto> findByActivoTrue(Pageable pageable);

    // Alerta de stock mínimo: productos activos cuyo stock actual <= stock mínimo
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stockActual <= p.stockMinimo")
    List<Producto> obtenerAlertasDeStock();
}
