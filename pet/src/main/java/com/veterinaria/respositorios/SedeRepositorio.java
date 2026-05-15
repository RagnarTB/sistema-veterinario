package com.veterinaria.respositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.veterinaria.modelos.Sede;

public interface SedeRepositorio extends JpaRepository<Sede, Long> {
    boolean existsByNombre(String nombre);
    List<Sede> findAllByActivoTrue();
}
