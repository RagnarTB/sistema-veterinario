package com.veterinaria.respositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.veterinaria.modelos.Proveedor;

public interface ProveedorRepositorio extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findByActivoTrue();
    boolean existsByRuc(String ruc);
}
