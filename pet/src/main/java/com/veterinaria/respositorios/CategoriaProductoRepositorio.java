package com.veterinaria.respositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.veterinaria.modelos.CategoriaProducto;

public interface CategoriaProductoRepositorio extends JpaRepository<CategoriaProducto, Long> {
    List<CategoriaProducto> findByActivoTrue();
    boolean existsByNombreIgnoreCase(String nombre);
}
