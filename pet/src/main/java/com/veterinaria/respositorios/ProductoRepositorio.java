package com.veterinaria.respositorios;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.modelos.Producto;

public interface ProductoRepositorio extends JpaRepository<Producto, Long> {

    // Para el módulo de "Nueva Venta": solo productos activos con paginación
    Page<Producto> findByActivoTrue(Pageable pageable);

    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    boolean existsByCategoriaId(Long categoriaId);
    boolean existsByUnidadCompraId(Long unidadId);
    boolean existsByUnidadVentaId(Long unidadId);
}
